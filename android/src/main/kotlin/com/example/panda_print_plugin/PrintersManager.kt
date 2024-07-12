package com.example.panda_print_plugin

import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.panda_print_plugin.models.Printer
import net.posprinter.posprinterface.IMyBinder
import net.posprinter.posprinterface.ProcessData
import net.posprinter.posprinterface.UiExecute
import net.posprinter.service.PosprinterService
import net.posprinter.utils.DataForSendToPrinterTSC
import net.posprinter.utils.DataForSendToPrinterTSC.text

@TargetApi(Build.VERSION_CODES.M)
class PrintersManager(
    private val appContext: Context,
    private val log: (msg: String) -> Unit
) {
    companion object {
        const val REQUEST_CODE_TURN_ON_BLUETOOTH = 123
        const val DISCOVER_MILIS = 4000L
    }

    private val bluetoothManager: BluetoothManager =
        appContext.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val handler = Handler(Looper.getMainLooper())

    private val _discoveredDevicesLD: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val discoveredDevices get() = _discoveredDevicesLD.value ?: listOf()
    val discoverPrintersLD: LiveData<List<Printer>> =
        Transformations.map(_discoveredDevicesLD) { devices ->
            devices
                .filter { isPrinter(it) }
                .map { Printer(it.name, it.address) }
        }

    lateinit var printerService: IMyBinder

    fun init() {
        setupBluetoothReceiver()
        setupPrinterService()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            printerService = p1 as IMyBinder
            log("Printer service connected")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            log("Printer service disconnected")
        }
    }

    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context, intent: Intent) {
            if (intent.action != BluetoothDevice.ACTION_FOUND) {
                return
            }
            val device: BluetoothDevice =
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
            if (!wasFound(device)) {
                val updatedDeivces = discoveredDevices
                    .toMutableList()
                    .apply { add(device) }
                _discoveredDevicesLD.postValue(updatedDeivces)
            }
        }
    }

    private fun wasFound(device: BluetoothDevice): Boolean {
        return discoveredDevices.any { it.address == device.address }
    }

    private fun setupPrinterService() {
        val intent = Intent(appContext, PosprinterService::class.java)
        appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun setupBluetoothReceiver() {
        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        appContext.registerReceiver(bluetoothReceiver, intentFilter)
    }

    fun onClose() {
        appContext.unregisterReceiver(bluetoothReceiver)
    }

    /**
     * Discover printers in 4 seconds
     *
     * @param onDiscoverComplete will be called on discovering complete with discovered printers result
     * */
    fun discoverPrinters(onDiscoverComplete: ((List<Printer>) -> Unit)? = null) {
        bluetoothAdapter.startDiscovery()
        handler.postDelayed(
            {
                bluetoothAdapter.cancelDiscovery()
                onDiscoverComplete?.invoke(discoverPrintersLD.value ?: listOf())
            },
            DISCOVER_MILIS
        )
    }

    private fun isPrinter(device: BluetoothDevice): Boolean {
        val deviceClass: Int = device.bluetoothClass.deviceClass
        val majorDeviceClass: Int = device.bluetoothClass.majorDeviceClass
        return majorDeviceClass == BluetoothClass.Device.Major.IMAGING
    }

    fun isBluetoothOn(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun turnOnBluetooth(activity: Activity) {
        val turnOnBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(turnOnBluetooth, REQUEST_CODE_TURN_ON_BLUETOOTH)
    }


    fun connectToPrinter(
        printerAddress: String,
        onSuccess: () -> Unit,
        onError: (error: Any) -> Unit
    ) {
        val printer = bluetoothAdapter.getRemoteDevice(printerAddress)

        // Bonding printer
        if (printer.bondState == BluetoothDevice.BOND_NONE) {
            log("Bonding to ${printer.address}")
            val success = printer.createBond()
            if (success) {
                log("Bonding successfully")
            } else {
                onError("Bonding failed, exit")
                log("Bonding failed, exit")
                return
            }
        }

        // Connect printer
        printerService.connectBtPort(printerAddress, object : UiExecute {
            override fun onsucess() {
                onSuccess()
            }

            override fun onfailed() {
                onError("Failed to connect printer")
            }
        })
    }

    fun printLoginQr(onSuccess: () -> Unit, onError: (error: Any) -> Unit) {
        printerService.writeDataByYouself(object : UiExecute {
            override fun onsucess() {
                onSuccess()
            }

            override fun onfailed() {
                onSuccess()
            }
        }, object : ProcessData {
            override fun processDataBeforeSend(): MutableList<ByteArray> {
                val printBytes = mutableListOf<ByteArray>().apply {
                    add(DataForSendToPrinterTSC.direction(1))
                    add(DataForSendToPrinterTSC.sizeBydot(850, 200))
//                    add(DataForSendToPrinterTSC.offSetBymm(-10.0))
//                    add(DataForSendToPrinterTSC.backFeed(180))
                    add(DataForSendToPrinterTSC.cls())
                    add(
                        CustomDataForSendToPrinter.textAlign(
                            16,
                            16,
                            "3",
                            0,
                            1,
                            1,
                            "Hello world",
                            2 // 2 center
                        )
                    )
                    add(DataForSendToPrinterTSC.print(1))
                    add(DataForSendToPrinterTSC.eoj())
                    add(DataForSendToPrinterTSC.cut())

                }
                return printBytes
            }
        })
    }

}
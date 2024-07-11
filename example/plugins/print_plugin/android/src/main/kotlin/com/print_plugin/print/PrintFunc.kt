package com.print_plugin.print

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.print_plugin.PrintPlugin
import com.print_plugin.PrintPlugin.Instance.foundBTDevice
import com.print_plugin.PrintPlugin.Instance.log
import com.print_plugin.PrintPlugin.Instance.permissionState
import com.print_plugin.PrintPlugin.Instance.updateBleStatus
import com.print_plugin.print.utils.PrintOrderUtils.Companion.printOrderBarcode
import com.print_plugin.print.utils.PrintOrderUtils.Companion.printOrderQR
import com.print_plugin.print.utils.PrintUtils.Companion.printErrorExecute
import com.print_plugin.print.model.DataBle
import com.print_plugin.print.model.PrintOrderParams
import com.print_plugin.print.model.QrCodeLoginParams
import com.print_plugin.print.utils.PrintOrderUtils
import com.print_plugin.print.utils.PrintOrderUtils.Companion.printLabBoxBarcode
import com.print_plugin.print.utils.PrintOrderUtils.Companion.printLabBoxQR
import com.print_plugin.print.utils.PrintOrderUtils.Companion.printQrCodeLogin
import com.print_plugin.utils.Conts
import net.posprinter.posprinterface.IMyBinder
import net.posprinter.posprinterface.UiExecute
import net.posprinter.service.PosprinterService
import net.posprinter.utils.BitmapToByteData
import net.posprinter.utils.DataForSendToPrinterPos80
import net.posprinter.utils.DataForSendToPrinterTSC
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt

class PrintFunc {
    //bindService connection
    var conn: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            //Bind successfully
            binder = iBinder as IMyBinder
            Log.e("binder", "connected")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.e("binder", "disconnected")
        }
    }

    fun initFunc(activity: Activity?) {
        val intent = Intent(activity, PosprinterService::class.java)
        activity?.bindService(intent, conn!!, Context.BIND_AUTO_CREATE)
    }

    fun destroy(activity: Activity?) {
        try {
            if (btReceiverRegistered) {
                btReceiverRegistered = false
                activity?.unregisterReceiver(btReceiver)
            }
            if (binderHasConnected) {
                binder!!.disconnectCurrentPort(object : UiExecute {
                    override fun onsucess() {}
                    override fun onfailed() {}
                })
            }
            if (conn != null) {
                activity?.unbindService(conn!!)
            }
        } catch (ignored: Exception) {
        }
    }

    @SuppressLint("MissingPermission")
    fun enableBT(activity: Activity?) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            //enable bluetooth if not
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity?.startActivityForResult(intent, Conts.ENABLE_BLUETOOTH)
        } else {
            permissionState(Conts.ENABLE_BLUETOOTH)
        }
    }

    @SuppressLint("MissingPermission")
    fun setBluetooth(
        activity: Activity?,
        isDiscover: Boolean,
        discoverTimeout: Int,
    ): List<DataBle> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return if (!bluetoothAdapter.isEnabled) {
            //enable bluetooth if not
            if (!isDiscover) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity?.startActivityForResult(intent, Conts.ENABLE_BLUETOOTH)
            }
            ArrayList()
        } else {
            if (isDiscover) {
                if (!btReceiverRegistered) {
                    //Register receiver to receive BT device
                    val filter = IntentFilter()
                    filter.addAction(BluetoothDevice.ACTION_FOUND)
                    filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                    activity?.registerReceiver(btReceiver, filter)
                    btReceiverRegistered = true
                }
                discoverBTDevice(bluetoothAdapter, discoverTimeout)
                ArrayList()
            } else {
                findAvailableDevice(bluetoothAdapter)
            }
        }
    }

    @SuppressLint("MissingPermission")
    val btReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device = intent.getParcelableExtra<BluetoothDevice>(
                    BluetoothDevice.EXTRA_DEVICE,
                )
                if (device != null) {
                    val name: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            device.alias
                        } catch (e: Exception) {
                            device.name
                        }
                    } else {
                        device.name
                    }
                    val deviceHardwareAddress = device.address // MAC address
                    if (name != null) foundBTDevice(
                        DataBle(
                            name,
                            deviceHardwareAddress,
                            device.bondState == BluetoothDevice.BOND_BONDED,
                            device.bluetoothClass.toString()
                        )
                    )
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(
                    BluetoothDevice.EXTRA_DEVICE,
                )
                if (device != null) {
                    if (device.bondState == BluetoothDevice.BOND_BONDED) {
                        log("Bonded with out deviceAwaitToBond")
                    }
                    if (device.bondState == BluetoothDevice.BOND_BONDED && deviceAwaitToBond != null) {
                        deviceAwaitToBond = null
                        connectBle(device.address, false)
                        log("Bonded")
                    } else if (device.bondState == BluetoothDevice.BOND_NONE) {
                        updateBleStatus(isSuccess = false, isDisconnected = false)
                        log("Bond none")
                    }
                } else {
                    log("Bonded with out device")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun discoverBTDevice(bluetoothAdapter: BluetoothAdapter, timeout: Int) {
        bluetoothAdapter.startDiscovery()
        log("Started discover...")
        //stop discover after timeout
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                bluetoothAdapter.cancelDiscovery()
            }
        }, timeout * 1000L)
    }

    @SuppressLint("MissingPermission")
    private fun findAvailableDevice(bluetoothAdapter: BluetoothAdapter): List<DataBle> {
        val bondedDevices = bluetoothAdapter.bondedDevices
        if (bluetoothAdapter.isDiscovering) {
            return ArrayList()
        }
        return if (bondedDevices.size > 0) {
            val list: MutableList<DataBle> = ArrayList()
            for (device in bondedDevices) {
                var name: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        device.alias
                    } catch (e: Exception) {
                        device.name
                    }
                } else {
                    device.name
                }
                val deviceHardwareAddress = device.address // MAC address
                list.add(
                    DataBle(
                        name, deviceHardwareAddress, true, device.bluetoothClass.toString()
                    )
                )
            }
            list
        } else {
            ArrayList()
        }
    }

    @SuppressLint("MissingPermission")
    fun connectBle(address: String?, disconnectCurrentAddress: Boolean?) {
        if (disconnectCurrentAddress == true && binderHasConnected) {
            binder!!.disconnectCurrentPort(object : UiExecute {
                override fun onsucess() {
                    //disconnected
                    updateBleStatus(true, true)
                    binderHasConnected = false
                }

                override fun onfailed() {
                    //not disconnected
                }
            })
        }
        if (address != "") {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val device = bluetoothAdapter.getRemoteDevice(address)
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                device.createBond()
                deviceAwaitToBond = device
                log("Device is not bone yet -> Bonding...")
            }
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                log("Device is boned -> Connecting...")
                binder!!.connectBtPort(address, object : UiExecute {
                    override fun onsucess() {
                        //connected
                        updateBleStatus(isSuccess = true, isDisconnected = false)
                        binderHasConnected = true
                        binder!!.write(DataForSendToPrinterPos80.openOrCloseAutoReturnPrintState(
                            0x1f
                        ), object : UiExecute {
                            override fun onsucess() {
                                binder!!.acceptdatafromprinter(object : UiExecute {
                                    override fun onsucess() {
                                        //connected printer
                                        updateBleStatus(
                                            isSuccess = true, isDisconnected = false
                                        )
                                        binderHasConnected = true
                                    }

                                    override fun onfailed() {
                                        //disconnected printer
                                        updateBleStatus(
                                            isSuccess = false, isDisconnected = true
                                        )
                                        binderHasConnected = false
                                        log("Connect Failed 3")
                                    }
                                })
                            }

                            override fun onfailed() {
                                //connect printer fail
                                updateBleStatus(isSuccess = false, isDisconnected = false)
                                binderHasConnected = false
                                log("Connect Failed 2")
                            }
                        })
                    }

                    override fun onfailed() {
                        //connect BT port fail
                        updateBleStatus(isSuccess = false, isDisconnected = false)
                        binderHasConnected = false
                        log("Connect Failed 1")
                    }
                })
            }
        }
    }

    suspend fun printQrCodeLogin(qrCodeLoginParams: QrCodeLoginParams) {
       PrintOrderUtils.printQrCodeLogin(qrCodeLoginParams)
    }
    suspend fun printOrder(printOrderParams: PrintOrderParams) {
        if (printOrderParams.form == "receipt") {
            if (printOrderParams.type == "barcode") {
                printOrderBarcode(printOrderParams)
            } else {
                printOrderQR(printOrderParams)
            }
        } else {
            if (printOrderParams.type == "barcode") {
                printLabBoxBarcode(printOrderParams)
            } else {
                printLabBoxQR(printOrderParams)
            }
        }

    }


    fun printImage(a: Bitmap) {
        val width = a.width.toFloat().roundToInt().toDouble()
        val height = a.height.toFloat().roundToInt().toDouble()

        //dpi printer 203
        val inch = height / 203
        val delay = (inch * 750).roundToInt()
        if (width > 0 && height > 0) {
            setupImageToPrint(a, width.toInt(), height.toInt(), delay.toInt())
        } else {
            PrintPlugin.updatePrintStatus(false, (4000 + 0).toLong())
        }
    }

    private fun setupImageToPrint(b: Bitmap?, width: Int, height: Int, delay: Int) {
        if (b == null) {
            PrintPlugin.updatePrintStatus(false, 0)
        } else {
            PrintFunc.binder!!.writeDataByYouself(printErrorExecute(delay)) {
                val list = ArrayList<ByteArray>()
                list.add(DataForSendToPrinterTSC.cls())
                list.add(DataForSendToPrinterTSC.sizeBydot(width, height + 70))
                list.add(DataForSendToPrinterTSC.cls())
                list.add(DataForSendToPrinterTSC.offSetBydot(0))
                list.add(DataForSendToPrinterTSC.gapByinch(0.0, 0.0))
                list.add(DataForSendToPrinterTSC.direction(1))
                list.add(DataForSendToPrinterTSC.density(7))
                list.add(DataForSendToPrinterTSC.speed(6.0))
                list.add(
                    DataForSendToPrinterTSC.bitmap(
                        5, 70, 0, b, BitmapToByteData.BmpType.Dithering
                    )
                )
                list.add(DataForSendToPrinterTSC.eoj())
                list.add(DataForSendToPrinterTSC.delay(1000))
                list.add(DataForSendToPrinterTSC.backFeed(240))
                list.add(DataForSendToPrinterTSC.eoj())
                list.add(DataForSendToPrinterTSC.print(1))
                list.add(DataForSendToPrinterTSC.eoj())
                list.add(DataForSendToPrinterTSC.cut())
                list.add(DataForSendToPrinterTSC.cls())
                list
            }
        }
    }

    companion object {
        var binder: IMyBinder? = null
        var deviceAwaitToBond: BluetoothDevice? = null
        var binderHasConnected = false
        var btReceiverRegistered = false
    }
}
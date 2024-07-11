package com.example.panda_print_plugin

import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.panda_print_plugin.models.Printer

@TargetApi(Build.VERSION_CODES.M)
class PrintersManager(private val appContext: Context) {
    companion object {
        const val REQUEST_CODE_TURN_ON_BLUETOOTH = 123
        const val DISCOVER_MILIS = 4000L
    }

    private val bluetoothManager: BluetoothManager = appContext.getSystemService(BluetoothManager::class.java)
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


    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context, intent: Intent) {
            if (intent.action != BluetoothDevice.ACTION_FOUND){
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

    fun registerReceiver() {
        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        appContext.registerReceiver(bluetoothReceiver, intentFilter)
    }

    fun unregisterReceiver() {
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
}
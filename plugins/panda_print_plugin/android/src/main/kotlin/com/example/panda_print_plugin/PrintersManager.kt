package com.example.panda_print_plugin

import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.panda_print_plugin.models.Printer

@TargetApi(Build.VERSION_CODES.M)
class PrintersManager(appContext: Context) {
    companion object {
        const val REQUEST_CODE_TURN_ON_BLUETOOTH = 123
    }

    val bluetoothManager: BluetoothManager = appContext.getSystemService(BluetoothManager::class.java);
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter;

    fun discoverPrinters(): List<Printer> {
        return bluetoothAdapter.bondedDevices.map {
            Printer(name = it.name, address = it.address)
        }
    }

    fun isBluetoothOn(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun turnOnBluetooth(activity: Activity) {
        val turnOnBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(turnOnBluetooth, REQUEST_CODE_TURN_ON_BLUETOOTH)
    }
}
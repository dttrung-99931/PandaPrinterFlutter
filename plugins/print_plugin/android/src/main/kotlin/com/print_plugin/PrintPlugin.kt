package com.print_plugin

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.print_plugin.print.PrintFunc
import com.print_plugin.print.model.PrintOrderParams
import com.print_plugin.print.model.QrCodeLoginParams
import com.print_plugin.print.utils.PrintUtils.Companion.checkLocation
import com.print_plugin.utils.Conts
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*


/** PrintPlugin */
class PrintPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity

    companion object Instance {
        private lateinit var channel: MethodChannel

        @JvmStatic
        fun updatePrintStatus(isSuccess: Boolean, delay: Long = 0) {
            if (isSuccess) {
                channel.invokeMethod("printSuccess", delay)

            } else {
                channel.invokeMethod("printFail", delay)
            }
        }

        @JvmStatic
        fun updateBleStatus(isSuccess: Boolean, isDisconnected: Boolean) {
            if (isDisconnected) {
                channel.invokeMethod("disconnect", null)
            } else {
                if (isSuccess) {
                    channel.invokeMethod("bleSuccess", null)
                } else {
                    channel.invokeMethod("bleFail", null)
                }
            }
        }

        @JvmStatic
        fun foundBTDevice(device: Any) {
            val gson = Gson()
            val json: String = gson.toJson(device)
            channel.invokeMethod("foundBTDevice", json)
        }

        @JvmStatic
        fun permissionState(state: Int) {
            channel.invokeMethod("PermissionState", state)
        }

        @JvmStatic
        fun log(message: String) {
            channel.invokeMethod("log", message)
        }
    }

    private var printFunc: PrintFunc = PrintFunc()

    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var activity: Activity? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = flutterPluginBinding;
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "print_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }

            "getListBle" -> {
                val list = PrintFunc().setBluetooth(activity, false, 0)
                val gson = Gson()
                val json: String = gson.toJson(list)
                return result.success(json)
            }

            "discoverBTDevices" -> {
                val timeout = call.argument<Int?>("timeout")
                val list = PrintFunc().setBluetooth(activity, true, timeout!!)
                val gson = Gson()
                val json: String = gson.toJson(list)
                return result.success(json)
            }

            "connectBle" -> {
                val address = call.argument<String>("address")
                val disconnectCurrentAddress = call.argument<Boolean>("disconnectCurrentAddress")
                printFunc.connectBle(address, disconnectCurrentAddress)
                return result.success(true)
            }

            "printImage" -> {
                val data = call.argument<ByteArray?>("data")
                val bitmap = BitmapFactory.decodeByteArray(data!!, 0, data.size)
                printFunc.printImage(bitmap)
                return result.success(true)
            }
            "printQrCodeLogin" -> {
                val json: Map<String, Any>? = call.arguments as Map<String, Any>
//                Log.d("", "onMethodCall: ${json.toString()}")
                val g = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create()
                val model = g.fromJson(JSONObject(json).toString(), QrCodeLoginParams::class.java)
                GlobalScope.launch(Dispatchers.Main) {
                    printFunc.printQrCodeLogin(model)
                }
                return result.success(true)
            }
            "printOrder" -> {
                val json: Map<String, Any>? = call.arguments as Map<String, Any>
//                Log.d("", "onMethodCall: ${json.toString()}")
                val g = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create()
                val model = g.fromJson(JSONObject(json).toString(), PrintOrderParams::class.java)
                GlobalScope.launch(Dispatchers.Main) {
                    printFunc.printOrder(model)
                }
                return result.success(true)
            }

            "requestPermissionBT" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (activity!!.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED && activity!!.checkCallingOrSelfPermission(
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        activity!!.requestPermissions(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ), Conts.PERMISSION_BLUETOOTH
                        )
                    } else {
                        permissionState(Conts.PERMISSION_BLUETOOTH)
                    }
                } else {
                    permissionState(Conts.PERMISSION_BLUETOOTH)
                }
                return result.success(true)
            }

            "requestPermissionLocation" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity!!.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && activity!!.checkCallingOrSelfPermission(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        activity!!.requestPermissions(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), Conts.PERMISSION_LOCATION
                        )
                    } else {
                        permissionState(Conts.PERMISSION_LOCATION)
                    }
                } else {
                    permissionState(Conts.PERMISSION_LOCATION)
                }
                return result.success(true)
            }

            "enableBT" -> {
                PrintFunc().enableBT(activity)
                return result.success(true)
            }

            "enableLocation" -> {
                checkLocation(activity)
                return result.success(true)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = binding
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        //bind service，get ImyBinder object
        printFunc.initFunc(binding.activity)
        binding.addActivityResultListener(this)
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        printFunc.destroy(activity)
        binding.addActivityResultListener(this)
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivity() {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray,
    ): Boolean {
        if (requestCode == Conts.PERMISSION_BLUETOOTH) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                log("PER_BT OK")
                permissionState(Conts.PERMISSION_BLUETOOTH)
                return true;
            } else {
                log("PER_BT NOT OK")
                permissionState(-Conts.PERMISSION_BLUETOOTH)
            }
        }
        if (requestCode == Conts.PERMISSION_LOCATION) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                log("PER_LOCATION OK")
                permissionState(Conts.PERMISSION_LOCATION)
                return true;
            } else {
                log("PER_LOCATION NOT OK")
                permissionState(-Conts.PERMISSION_LOCATION)
            }
        }
        return false;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == Conts.ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                log("BT OK")
                permissionState(Conts.ENABLE_BLUETOOTH)
                return true;
            } else {
                log("BT NOT OK")
                permissionState(-Conts.ENABLE_BLUETOOTH)
            }
        }
        if (requestCode == Conts.ENABLE_LOCATION) {
            permissionState(Conts.ENABLE_LOCATION)
        }
        return false;
    }
}

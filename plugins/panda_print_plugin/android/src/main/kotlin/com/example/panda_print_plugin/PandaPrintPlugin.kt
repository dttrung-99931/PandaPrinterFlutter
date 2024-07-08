package com.example.panda_print_plugin

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** PandaPrintPlugin */
class PandaPrintPlugin: PandaPrintActivityAware(), FlutterPlugin, MethodCallHandler, PluginRegistry.ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var printersManager: PrintersManager
  private val gson: Gson = Gson()
  private lateinit var methodChannelResult: Result

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "panda_print_plugin")
    channel.setMethodCallHandler(this)
    printersManager = PrintersManager(flutterPluginBinding.applicationContext)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    this.methodChannelResult = result
    when (call.method ){
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "discoverPrinters" -> handleDiscoveringPrinters()
      "requestPermissions" -> requestPrinterPermissions(result)
      else -> result.notImplemented()
    }
  }

  private fun handleDiscoveringPrinters() {
    if (!printersManager.isBluetoothOn()){
      printersManager.turnOnBluetooth(activity)
      log("Turning on bluetooth!")
      return
    }
    discoverPrinter()
  }

  fun discoverPrinter(){
    val printers = printersManager.discoverPrinters()
    methodChannelResult.success(gson.toJson(printers))
  }

  private fun requestPrinterPermissions(result: Result) {
    //TODO:
  }

  private fun log(msg: String){
    Log.d("PandaPrintPlugin", msg)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    if (requestCode == PrintersManager.REQUEST_CODE_TURN_ON_BLUETOOTH){
      if (resultCode == Activity.RESULT_OK){
          log("Turn on bluetooth successfully. Discovering")
          discoverPrinter()
      } else {
        log("User disagree turning on bluetooth")
      }
    }
    return false
  }

}

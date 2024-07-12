package com.example.panda_print_plugin

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import com.example.panda_print_plugin.models.Printer
import com.example.panda_print_plugin.models.PrinterManagerStatus
import com.google.gson.Gson
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** PandaPrintPlugin */
class PandaPrintPlugin: PandaPrintActivityAware(), FlutterPlugin, MethodCallHandler, PluginRegistry.ActivityResultListener {
  companion object {
      const val PANDA_PRINT_CHANNEL = "panda_print_plugin"
      const val DISCOVERED_PRINTERS_EVT_CHANNEL = "discovered_printers_event_channel"
      const val STATUS_EVT_CHANNEL = "status_event_channel"
      const val FLUTTER_LOG_METHOD = "logd"
      const val CONNECT_PRINTER_METHOD = "connectPrinter"
      const val PRINT_LOGIN_QR_METHOD = "printLoginQR"
  }

  /** Channels for communication with Flutter */
  private lateinit var channel : MethodChannel
  private lateinit var methodChannelResult: Result

  private lateinit var discoveredPrintersChannel : EventChannel
  private var discoveredPrintersSink: EventChannel.EventSink? = null

  private lateinit var statusChannel : EventChannel
  private var statusSink: EventChannel.EventSink? = null

  /** Printer manager */
  private lateinit var printersManager: PrintersManager
  private val gson: Gson = Gson()
  private val onPrintersDiscovered = Observer(this::notifyDiscoveredPrinters)

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    setupFlutterChannels(flutterPluginBinding)
    setupNativePrinterManager(flutterPluginBinding)
    log("Settup panda printer complete!")
  }

  private fun setupFlutterChannels(binding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(binding.binaryMessenger, PANDA_PRINT_CHANNEL)
      .apply {
        setMethodCallHandler(this@PandaPrintPlugin)
      }
    discoveredPrintersChannel = EventChannel(binding.binaryMessenger, DISCOVERED_PRINTERS_EVT_CHANNEL).also {
      it.setStreamHandler(object: EventChannel.StreamHandler{
        override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
          discoveredPrintersSink = events
        }

        override fun onCancel(arguments: Any?) {
          discoveredPrintersSink = null
        }
      })
    }
    statusChannel = EventChannel(binding.binaryMessenger, STATUS_EVT_CHANNEL).also {
      it.setStreamHandler(object: EventChannel.StreamHandler{
        override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
          statusSink = events
        }

        override fun onCancel(arguments: Any?) {
          statusSink = null
        }
      })
    }
  }

  private fun notifyDiscoveredPrinters(printers: List<Printer>){
    discoveredPrintersSink?.success(gson.toJson(printers))
  }

  private fun notifyStatus(printerManagerStatus: PrinterManagerStatus){
    statusSink?.success(gson.toJson(printerManagerStatus))
  }


  private fun setupNativePrinterManager(binding: FlutterPlugin.FlutterPluginBinding) {
    printersManager = PrintersManager(binding.applicationContext, this::log)
      .also {
        it.init()
      }
    printersManager.discoverPrintersLD.observeForever(onPrintersDiscovered)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    discoveredPrintersChannel.setStreamHandler(null)
    printersManager.onClose()
    printersManager.discoverPrintersLD.removeObserver(onPrintersDiscovered)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    this.methodChannelResult = result
    when (call.method ){
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "discoverPrinters" -> handleDiscoveringPrinters()
      "requestPermissions" -> requestPrinterPermissions(result)
      CONNECT_PRINTER_METHOD -> connectPrinter(call)
      PRINT_LOGIN_QR_METHOD -> printLoginQR(call)
      else -> result.notImplemented()
    }
  }

  private fun printLoginQR(call: MethodCall) {
    printersManager.printLoginQr({
      responseSuccess("Print successfully")
    }, {
      responseError("Print failure")
    })
  }

  private fun connectPrinter(call: MethodCall) {
    val printerAddress = call.arguments?.toString()
    if (printerAddress.isNullOrEmpty()){
      responseError("Printer address is empty")
      return
    }
    printersManager.connectToPrinter(printerAddress, {
      responseSuccess("Connect successfully")
    }, { error ->
      responseError(error.toString())
    })
  }

  private fun responseSuccess(data: Any){
    methodChannelResult.success(data)
  }

  private fun responseError(message: String, code: String = "", details: Any = Unit){
    log(message.toString())
    methodChannelResult.error(code, message, details)
  }

  private fun handleDiscoveringPrinters() {
    if (printersManager.isBluetoothOn()){
      discoverPrinter()
    } else {
      printersManager.turnOnBluetooth(activity)
      log("Turning on bluetooth!")
    }
  }

  private fun discoverPrinter(){
    notifyStatus(PrinterManagerStatus.DISCOVERING)
    printersManager.discoverPrinters {
      notifyStatus(PrinterManagerStatus.DISCOVER_COMPLETE)
      methodChannelResult.success(gson.toJson(it))
    }
  }

  private fun requestPrinterPermissions(result: Result) {
    //TODO:
  }

  private fun log(msg: String){
    channel.invokeMethod(FLUTTER_LOG_METHOD, msg)
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

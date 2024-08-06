import Flutter
import UIKit
import XYBLEManagerDelegate

public class PandaPrintPlugin: NSObject, FlutterPlugin, FlutterStreamHandler, XYBLEManagerDelegate
 {
    static var channel: FlutterMethodChannel!
    static var discoverPrintersChannel: FlutterEventChannel!
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let instance = PandaPrintPlugin()

        channel = FlutterMethodChannel(
            name: "panda_print_plugin",
            binaryMessenger: registrar.messenger()
        )
        registrar.addMethodCallDelegate(instance, channel: channel!)
        
        discoverPrintersChannel = FlutterEventChannel(
            name: "discovered_printers_event_channel",
            binaryMessenger: registrar.messenger()
        )
        discoverPrintersChannel.setStreamHandler(instance)
    }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
        case "discoverPrinters":
            log("Discover printers")
        case "connectPrinter":
            log("Connect to a printer")
        default:
            return result(FlutterMethodNotImplemented)
    }
    log("Done setup method ios");
  }
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        return nil
    }
    
    
    private func log(_ msg: String){
        PandaPrintPlugin.channel.invokeMethod(
            "logd",
            arguments: msg
        );
    }
    
    func xYdidUpdatePeripheralList(_ peripherals: [Any]!, rssiList: [Any]!) {
       // HandleCallMethod.channel?.invokeMethod("err",arguments: nil)
    }
    
    func xYdidConnect(_ peripheral: CBPeripheral!) {
//        HandleCallMethod.channel?.invokeMethod("connected",arguments: nil)
      
    }
   
    func xYdidFail(toConnect peripheral: CBPeripheral!, error: Error!) {
//        HandleCallMethod.channel?.invokeMethod("connectFalse",arguments: error)
    }
    
    func xYdidDisconnectPeripheral(_ peripheral: CBPeripheral!, isAutoDisconnect: Bool) {
//        HandleCallMethod.channel?.invokeMethod("disconnectFromPrinterDevice",arguments: isAutoDisconnect)
    }
    
    func xYdidWriteValue(for character: CBCharacteristic!, error: Error!) {
//        HandleCallMethod.channel?.invokeMethod("writingValue",arguments: error)

    }
    
}

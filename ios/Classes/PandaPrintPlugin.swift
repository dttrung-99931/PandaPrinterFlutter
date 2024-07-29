import Flutter
import UIKit

public class PandaPrintPlugin: NSObject, FlutterPlugin, FlutterStreamHandler {
    // Channel names
    static let discoveredPrinterEventName = "discovered_printers_event_channel";
    static let statusEvtChannelName = "status_event_channel";
    static let pluginChannelName = "panda_print_plugin";
    static let logMethodName = "logd";
    // Channel method names
    static let discoverPrintersMethod = "discoverPrinters";
    static let requestPermissionsMethod = "requestPermissions";
    static let connectPrinterMethod = "connectPrinter";
    static let printLoginQRMethod = "printLoginQR";

    static var channel: FlutterMethodChannel!
    static var discoverPrintersChannel: FlutterEventChannel!
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let instance = PandaPrintPlugin()

        channel = FlutterMethodChannel(
            name: pluginChannelName,
            binaryMessenger: registrar.messenger()
        )
        registrar.addMethodCallDelegate(instance, channel: channel!)
        
        discoverPrintersChannel = FlutterEventChannel(
            name: discoveredPrinterEventName, 
            binaryMessenger: registrar.messenger()
        )
        discoverPrintersChannel.setStreamHandler(instance)
    }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "discoverPrinters":
        log("Discovering printers");
    default:
      result(FlutterMethodNotImplemented)
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
            PandaPrintPlugin.logMethodName,
            arguments: msg
        );
    }
}

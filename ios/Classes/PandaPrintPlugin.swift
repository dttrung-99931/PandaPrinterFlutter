import Flutter
import UIKit

public class PandaPrintPlugin: NSObject, FlutterPlugin, FlutterStreamHandler {
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
}

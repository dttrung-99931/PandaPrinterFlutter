import 'package:dartz/dartz.dart';
import 'package:panda_print_plugin/models/panda_printer.dart';
import 'package:panda_print_plugin/models/printer_error.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'panda_print_plugin_android.dart';

abstract class PandaPrintPlugin extends PlatformInterface {
  /// Constructs a PandaPrintPluginPlatform.
  PandaPrintPlugin() : super(token: _token);

  static final Object _token = Object();

  static PandaPrintPlugin _instance = PandaPrintPluginAndroid();

  /// The default instance of [PandaPrintPlugin] to use.
  ///
  /// Defaults to [PandaPrintPluginAndroid].
  static PandaPrintPlugin get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [PandaPrintPlugin] when
  /// they register themselves.
  static set instance(PandaPrintPlugin instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Stream<List<PandaPrinter>> get discoveredPrintersStream;

  Future<List<PandaPrinter>> discoverPrinters() {
    throw UnimplementedError('discoverPrinters() has not been implemented.');
  }

  Future<void> init() {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future<Either<PrinterError, void>> connectPrinter(String printerAddress) {
    throw UnimplementedError('connectPrinter() has not been implemented.');
  }

  Future<Either<PrinterError, void>> printLoginQR(String loginQrCode) {
    throw UnimplementedError('printLoginQR() has not been implemented.');
  }

  // TODO: Connnect printer
  // lookUpPrinters
  // print
}

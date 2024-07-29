import 'dart:developer';

import 'package:dartz/dartz.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:panda_print_plugin/models/panda_printer.dart';
import 'package:panda_print_plugin/models/printer_error.dart';
import 'package:panda_print_plugin/models/printer_manager_status.dart';

import 'panda_print_plugin.dart';

/// An implementation of [PandaPrintPlugin] that uses method channels.
class PandaPrintPluginIOS extends PandaPrintPlugin {
  // Channel names
  static const discoveredPrinterEventName = 'discovered_printers_event_channel';
  static const statusEvtChannelName = 'status_event_channel';
  static const pluginChannelName = 'panda_print_plugin';
  static const logMethodName = 'logd';
  // Channel method names
  static const discoverPrintersMethod = 'discoverPrinters';
  static const requestPermissionsMethod = 'requestPermissions';
  static const connectPrinterMethod = 'connectPrinter';
  static const printLoginQRMethod = 'printLoginQR';

  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final channel = const MethodChannel(pluginChannelName);

  @visibleForTesting
  final discoveredPrintersEvtChannel =
      const EventChannel(discoveredPrinterEventName);

  @visibleForTesting
  final statusEvtChannel = const EventChannel(statusEvtChannelName);

  @override
  Stream<List<PandaPrinter>> get discoveredPrintersStream =>
      discoveredPrintersEvtChannel.receiveBroadcastStream().map(
        (event) {
          return PandaPrinter.fromJsons(event.toString());
        },
      );

  Stream<PrinterManagerStatus> get statusStream =>
      statusEvtChannel.receiveBroadcastStream().map(
        (event) {
          return PrinterManagerStatus.fromJson(event.toString());
        },
      );

  @override
  Future<void> init() async {
    // final bool isGranted = await _requestPrinterPermissions();
    // log('Printer permission granted: $isGranted');
    channel.setMethodCallHandler(_nativeMethodCallHandler);
  }

  // Future<bool> _requestPrinterPermissions() async {
  //   channel.invokeMethod(requestPermissionsMethod);
  // }

  @override
  Future<List<PandaPrinter>> discoverPrinters() async {
    String? printersJson = await channel.invokeMethod(discoverPrintersMethod);
    return printersJson != null ? PandaPrinter.fromJsons(printersJson) : [];
  }

  Future _nativeMethodCallHandler(MethodCall call) async {
    switch (call.method) {
      case logMethodName:
        log(call.arguments.toString());
        break;
    }
  }

  @override
  Future<Either<PrinterError, void>> connectPrinter(
      String printerAddress) async {
    try {
      await channel.invokeMethod(connectPrinterMethod, printerAddress);
      return const Right(null);
    } catch (e) {
      return Left(ConnectPrinterError(message: e.toString()));
    }
  }

  @override
  Future<Either<PrinterError, void>> printLoginQR(String loginQrCode) async {
    try {
      await channel.invokeMethod(printLoginQRMethod, loginQrCode);
      return const Right(null);
    } catch (e) {
      return Left(PrintError(message: e.toString()));
    }
  }
}

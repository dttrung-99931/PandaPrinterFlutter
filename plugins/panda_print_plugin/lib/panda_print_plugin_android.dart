import 'dart:convert';
import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:panda_print_plugin/models/panda_printer.dart';

import 'panda_print_plugin.dart';

/// An implementation of [PandaPrintPlugin] that uses method channels.
class PandaPrintPluginAndroid extends PandaPrintPlugin {
  static const discoverPrintersMethod = 'discoverPrinters';
  static const requestPermissionsMethod = 'requestPermissions';

  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final channel = const MethodChannel('panda_print_plugin');

  @override
  Future<void> init() async {
    final bool isGranted = await _requestPrinterPermissions();
    log('Printer permission granted: $isGranted');
  }

  Future<bool> _requestPrinterPermissions() async {
    return await channel.invokeMethod(requestPermissionsMethod);
  }

  @override
  Future<List<PandaPrinter>> discoverPrinters() async {
    String printersJsonStr = await channel.invokeMethod(discoverPrintersMethod);
    List<dynamic> printersJson = jsonDecode(printersJsonStr);
    return printersJson.map((e) => PandaPrinter.fromMap(e)).toList();
  }
}

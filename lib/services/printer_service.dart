import 'dart:async';
import 'dart:convert';
import 'dart:developer';

import 'package:dartz/dartz.dart';
import 'package:flutter/foundation.dart';
import 'package:panda_printer_example/models/app_error.dart';
import 'package:panda_printer_example/models/printer_model.dart';
import 'package:panda_printer_example/utils/ble_utils.dart';
import 'package:panda_printer_example/utils/extensions/list_extension.dart';
import 'package:print_plugin/print_plugin.dart';

class PrinterService extends ChangeNotifier {
  PrinterService() {
    setListener();
  }
  final List<PrinterModel> foundPrinters = [];

  void setListener() {
    PrintPlugin.listenChannelEvent(onPrintSuccess: () {
      log('print success');
    }, onPrintFail: () {
      log('print failed');
    }, onConnectSuccess: () {
      log('Connect sucess');
    }, onConnectFail: () {
      log('Connect failed');
    }, onDisconnected: () {
      log('Disconnected');
    });
  }

  Future<Either<AppError, List<PrinterModel>>> lookUpPrinters() async {
    if (!await BleUtils.requestPermissions()) {
      return Left(PrinterError(message: 'Bluetooth permissions need to be granted'));
    }
    String printersJson = await PrintPlugin.discoverBTDevices();
    List<PrinterModel> printers = List.from(jsonDecode(printersJson)).mapList((json) => PrinterModel.fromMap(json));
    return Right(printers);
  }

  Future<Either<AppError, void>> connectPrinter(PrinterModel printer) async {
    bool success = await PrintPlugin.connectBle(printer.address);
    return success
        ? const Right(null) // null for void
        : Left(PrinterError(message: 'Connect to printer ${printer.address} failed'));
  }

  Future<Either<AppError, void>> print() async {
    PrintPlugin.printerFilter("Test");
    return const Right(null);
  }
}

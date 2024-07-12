import 'dart:async';

import 'package:dartz/dartz.dart';
import 'package:flutter/foundation.dart';
import 'package:panda_print_plugin/models/panda_printer.dart';
import 'package:panda_print_plugin/panda_print.dart';
import 'package:panda_printer_example/models/app_error.dart';
import 'package:panda_printer_example/models/printer_model.dart';
import 'package:panda_printer_example/utils/ble_utils.dart';
import 'package:panda_printer_example/utils/extensions/list_extension.dart';

class PrinterService extends ChangeNotifier {
  PrinterService();
  final Stream<List<PrinterModel>> foundPrintersStream = PandaPrint.discoveredPrintersStream.map(
    (List<PandaPrinter> found) => found.mapList(PrinterModel.fromPandaPrinter),
  );
  late Completer<bool> _printSuccessCompleter;

  Future<void> initPrinterPlugin() async {
    await PandaPrint.init();
  }

  void setListener() {
    // PrintPlugin.listenChannelEvent(onPrintSuccess: () {
    //   log('print success');
    //   _printSuccessCompleter.completeIfNot(true);
    // }, onPrintFail: () {
    //   log('print failed');
    //   _printSuccessCompleter.completeIfNot(false);
    // }, onConnectSuccess: () {
    //   log('Connect sucess');
    // }, onConnectFail: () {
    //   log('Connect failed');
    // }, onDisconnected: () {
    //   log('Disconnected');
    // });
  }

  Future<Either<AppError, List<PrinterModel>>> lookUpPrinters() async {
    if (!await BleUtils.requestPermissions()) {
      return Left(PrinterError(message: 'Bluetooth permissions need to be granted'));
    }
    List<PandaPrinter> pandaPrinters = await PandaPrint.discoverPrinters();
    List<PrinterModel> printers = pandaPrinters.mapList(PrinterModel.fromPandaPrinter);
    return Right(printers);
  }

  Future<Either<AppError, void>> connectPrinter(PrinterModel printer) async {
    final result = await PandaPrint.connectToPrinter(printer.address);
    return result.leftMap(
      (l) {
        return AppError(message: l.message);
      },
    );
  }

  Future<Either<AppError, void>> print() async {
    final result = await PandaPrint.printLoginQR('Login QR');
    return result.leftMap(
      (l) {
        return AppError(message: l.message);
      },
    );
  }
}

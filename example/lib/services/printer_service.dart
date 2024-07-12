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
  PrinterService() {
    setListener();
  }
  final Stream<List<PrinterModel>> foundPrintersStream = PandaPrint.discoveredPrintersStream.map(
    (List<PandaPrinter> found) => found.mapList(PrinterModel.fromPandaPrinter),
  );
  late Completer<bool> _printSuccessCompleter;

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
    // bool success = await PrintPlugin.connectBle(printer.address);
    // return success
    //     ? const Right(null) // null for void
    //     : Left(PrinterError(message: 'Connect to printer ${printer.address} failed'));
    return Right(null);
  }

  Future<Either<AppError, void>> print() async {
    return Right(null);

    // _printSuccessCompleter = Completer();
    // PrintPlugin.printerFilter('aaa');
    // // PrintPlugin.printQrCodeLogin({
    // //   'title': 'Test',
    // //   'data': 'Test',
    // //   'content': 'Test',
    // // });
    // return await _printSuccessCompleter.future ? const Right(null) : Left(PrinterError(message: 'Print failed'));
  }
}

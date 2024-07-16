import 'dart:async';

import 'package:dartz/dartz.dart';
import 'package:flutter/foundation.dart';
import 'package:panda_print_plugin/models/panda_printer.dart';
import 'package:panda_print_plugin/panda_print.dart';
import 'package:panda_print_plugin/panda_print_storage.dart';
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

  Future<Either<AppError, void>> connectPrinterByAddress(String address) async {
    Either<AppError, List<PrinterModel>> result = await lookUpPrinters();
    PrinterModel? matchPrinter;
    result.fold(
      (l) {
        matchPrinter = null;
      },
      (List<PrinterModel> printers) {
        matchPrinter = printers.firstWhereOrNull((element) => element.address == address);
      },
    );
    if (matchPrinter != null) {
      return connectPrinter(matchPrinter!);
    }
    return Left(PrinterError(message: 'Not found printer $address'));
  }

  Future<Either<AppError, PrinterModel>> lookUpPrinterByAddress(String address) async {
    Either<AppError, List<PrinterModel>> result = await lookUpPrinters();
    return result.fold(
      (AppError l) {
        return Left(l);
      },
      (List<PrinterModel> printers) {
        PrinterModel? matchPrinter = printers.firstWhereOrNull((element) => element.address == address);
        return matchPrinter != null ? Right(matchPrinter) : Left(AppError(message: 'Not found printer'));
      },
    );
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

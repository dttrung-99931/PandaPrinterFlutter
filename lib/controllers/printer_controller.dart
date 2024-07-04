import 'package:dartz/dartz.dart';
import 'package:flutter/foundation.dart';
import 'package:panda_printer_example/models/app_error.dart';
import 'package:panda_printer_example/models/printer_model.dart';
import 'package:panda_printer_example/services/printer_service.dart';
import 'package:panda_printer_example/utils/extensions/list_extension.dart';
import 'package:panda_printer_example/utils/loading_utils.dart';

class PrinterController extends ChangeNotifier {
  final PrinterService _service = PrinterService();

  AppError? _error;
  AppError? get error => _error;

  PrinterModel? _connectedPrinter;
  PrinterModel? get connectedPrinter => _connectedPrinter;

  final List<PrinterModel> foundPrinters = [];

  Future<void> lookUpPrinters() async {
    showLoading();
    Either<AppError, List<PrinterModel>> result = await _service.lookUpPrinters();
    result.fold(
      (error) {
        _error = error;
        notifyListeners();
      },
      (List<PrinterModel> printers) {
        foundPrinters.assignAll(printers);
        notifyListeners();
      },
    );
    hideLoading();
  }

  Future<void> connectPrinter(PrinterModel item) async {
    showLoading();
    Either<AppError, void> result = await _service.connectPrinter(item);
    result.fold(
      (error) {
        _error = error;
      },
      (_) {
        _connectedPrinter = item;
      },
    );
    notifyListeners();
    hideLoading();
  }

  void print() {
    load(() async {
      Either<AppError, void> result = await _service.print();
      result.fold(
        (error) {
          _error = error;
        },
        (_) {},
      );
    });
  }

  Future<T> load<T>(Future<T> Function() action) async {
    T result;
    showLoading();
    result = await action();
    hideLoading();
    return result;
  }
}

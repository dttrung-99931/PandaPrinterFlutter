import 'package:panda_printer_example/controllers/base_controller.dart';
import 'package:panda_printer_example/models/printer_model.dart';
import 'package:panda_printer_example/services/printer_service.dart';
import 'package:panda_printer_example/utils/extensions/list_extension.dart';

class PrinterController extends BaseController {
  final PrinterService _service = PrinterService();

  PrinterModel? _connectedPrinter;
  PrinterModel? get connectedPrinter => _connectedPrinter;

  final List<PrinterModel> foundPrinters = [];

  Future<void> lookUpPrinters() async {
    await handleServiceResult(
      serviceResult: _service.lookUpPrinters(),
      onSuccess: (List<PrinterModel> result) {
        foundPrinters.assignAll(result);
      },
    );
    notifyListeners();
  }

  Future<void> connectPrinter(PrinterModel item) async {
    await handleServiceResult(
      serviceResult: _service.connectPrinter(item),
      onSuccess: (result) {
        _connectedPrinter = item;
      },
    );
    notifyListeners();
  }

  Future<void> print() async {
    await handleServiceResult(
      serviceResult: _service.print(),
      onSuccess: (result) {},
    );
  }
}

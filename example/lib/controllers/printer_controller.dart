import 'package:panda_printer_example/controllers/base_controller.dart';
import 'package:panda_printer_example/models/printer_model.dart';
import 'package:panda_printer_example/services/printer_service.dart';
import 'package:panda_printer_example/utils/extensions/list_extension.dart';
import 'package:panda_printer_example/utils/mixins/disposable_mixin.dart';

class PrinterController extends BaseController with DisposableMixin {
  PrinterController() {
    _listenFoundPrinters();
  }

  final PrinterService _service = PrinterService();

  PrinterModel? _connectedPrinter;
  PrinterModel? get connectedPrinter => _connectedPrinter;

  final List<PrinterModel> foundPrinters = [];

  bool isInitCompleted = false;

  Future<void> init() async {
    await _service.initPrinterPlugin();
    isInitCompleted = true;
    // await load(
    //   () async {},
    // );
    notifyListeners();
  }

  void _listenFoundPrinters() {
    addSubscription(_service.foundPrintersStream.listen(
      (List<PrinterModel> found) {
        foundPrinters.assignAll(found);
        notifyListeners();
      },
    ));
  }

  Future<void> lookUpPrinters() async {
    await handleServiceResult(
      /// The lookup printers will be handled in [_listenFoundPrinters]
      serviceResult: _service.lookUpPrinters(),
      onSuccess: (_) {},
    );
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

  @override
  void dispose() {
    disposeAllSubscriptions();
    super.dispose();
  }
}

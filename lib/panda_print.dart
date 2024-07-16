// You have generated a new plugin project without specifying the `--platforms`
// flag. A plugin project with no platform support was generated. To add a
// platform, run `flutter create -t plugin --platforms <platforms> .` under the
// same directory. You can also find a detailed instruction on how to add
// platforms in the `pubspec.yaml` at
// https://flutter.dev/docs/development/packages-and-plugins/developing-packages#plugin-platforms.

import 'package:dartz/dartz.dart';
import 'package:panda_print_plugin/models/panda_printer.dart';
import 'package:panda_print_plugin/models/printer_error.dart';
import 'package:panda_print_plugin/panda_print_storage.dart';

import 'panda_print_plugin.dart';

class PandaPrint {
  static Stream<List<PandaPrinter>> get discoveredPrintersStream {
    return PandaPrintPlugin.instance.discoveredPrintersStream;
  }

  static Future<List<PandaPrinter>> discoverPrinters() {
    return PandaPrintPlugin.instance.discoverPrinters();
  }

  static Future<Either<PrinterError, void>> connectToPrinter(String printerAddress) async {
    Either<PrinterError, void> result = await PandaPrintPlugin.instance.connectPrinter(printerAddress);
    if (result.isRight()) {
      await PandaPrintStorage.saveConnectedPrinterAddress(printerAddress);
    } else {
      await PandaPrintStorage.deleteConnectedPrinterAddress();
    }
    return result;
  }

  static Future<Either<PrinterError, void>> printLoginQR(String loginQrCode) {
    return PandaPrintPlugin.instance.printLoginQR(loginQrCode);
  }

  static Future<void> init() async {
    await PandaPrintStorage.init();
    return PandaPrintPlugin.instance.init();
  }
}

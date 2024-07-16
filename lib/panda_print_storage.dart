import 'package:hive_flutter/adapters.dart';

class PandaPrintStorage {
  PandaPrintStorage._();
  // static PandaPrintStorage? _instance;
  // static PandaPrintStorage get instance {
  //   _instance ??= PandaPrintStorage._();
  //   return _instance!;
  // }
  static const String boxName = 'panda_print_box';
  static const String keyConnnectedPrinterAddr = 'keyConnnectedPrinter';
  static Box? __storage;
  static Box get _storage {
    if (__storage == null) {
      throw 'PandaPrinterStorage has not been initilized. Call PandaPrintStorage.init to init';
    }
    return __storage!;
  }

  static Future<void> init() async {
    await Hive.initFlutter();
    __storage = await Hive.openBox(boxName);
  }

  static String? get connectedPrinterAddress => _storage.get(keyConnnectedPrinterAddr);
  static Future<void> saveConnectedPrinterAddress(String address) async {
    await _storage.put(keyConnnectedPrinterAddr, address);
  }

  static Future<void> deleteConnectedPrinterAddress() async {
    await _storage.delete(keyConnnectedPrinterAddr);
  }
}

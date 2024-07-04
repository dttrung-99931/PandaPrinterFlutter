// ignore_for_file: unnecessary_import, constant_identifier_names

import 'dart:async';
import 'dart:convert';
import 'dart:developer';
import 'dart:io';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_image_compress/flutter_image_compress.dart';
import 'package:path_provider/path_provider.dart';

const int BT_OK = 1;
const int BT_NOT_OK = -1;
const int LOCATION_SETTING_DONE = 2;
const int PER_BT_OK = 11;
const int PER_BT_NOT_OK = -11;
const int PER_LOCATION_OK = 22;
const int PER_LOCATION_NOT_OK = -22;

class PrintPlugin {
  static const MethodChannel _channel = MethodChannel('print_plugin');
  static List<Map<String, dynamic>> queue = [];
  static Completer<dynamic>? _discoverCompleter;
  static Completer<bool>? _perBTCompleter;
  static Completer<bool>? _perLocationCompleter;
  static Completer<bool>? _btCompleter;
  static Completer<bool>? _locationCompleter;
  static Timer? discoverTimer;
  static List discoverResult = [];
  static List discoverAllResult = [];
  static StreamController discoverStreamController = StreamController<List>();
  static Stream get discoverStream => discoverStreamController.stream;
  static StreamSubscription? discoverStreamSub;
  static Timer? printTimer;

  static void listenChannelEvent({
    required VoidCallback onPrintSuccess,
    required VoidCallback onPrintFail,
    required VoidCallback onConnectSuccess,
    required VoidCallback onConnectFail,
    required VoidCallback onDisconnected,
  }) {
    _channel.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'log':
          var message = call.arguments as String;
          log(message, name: 'PRINTER-PLUGIN');
          break;
        case 'PermissionState':
          var result = call.arguments as int;
          if (result == BT_OK) {
            log('PermissionState BT_OK');
            _btCompleter?.complete(true);
          } else if (result == BT_NOT_OK) {
            log('PermissionState BT_NOT_OK');
            _btCompleter?.complete(false);
          } else if (result == PER_BT_OK) {
            log('PermissionState PER_BT_OK');
            _perBTCompleter?.complete(true);
          } else if (result == PER_BT_NOT_OK) {
            log('PermissionState PER_BT_NOT_OK');
            _perBTCompleter?.complete(false);
          } else if (result == LOCATION_SETTING_DONE) {
            log('PermissionState LOCATION_SETTING_DONE');
            _locationCompleter?.complete(true);
          } else if (result == PER_LOCATION_OK) {
            log('PermissionState PER_LOCATION_OK');
            _perLocationCompleter?.complete(true);
          } else if (result == PER_LOCATION_NOT_OK) {
            log('PermissionState PER_LOCATION_NOT_OK');
            _perLocationCompleter?.complete(false);
          }
          break;
        case 'foundBTDevice':
          var device = jsonDecode(call.arguments) as Map;
          log('Found: $device');
          discoverAllResult.add(device);
          if (printerFilter(device['btClass'] as String) &&
              discoverResult.where((e) => e['address'] == device['address']).isEmpty) {
            discoverStreamController.sink.add(discoverResult);
            discoverResult.add(device);
          }
          break;
        case 'printSuccess':
          var delay = call.arguments as int;
          if (queue.isNotEmpty) queue.removeAt(0);

          printTimer = Timer(
            Duration(milliseconds: delay),
            () {
              if (queue.isEmpty) {
                onPrintSuccess();
              } else {
                _printOrderItem();
              }
            },
          );
          break;
        case 'printFail':
          printTimer?.cancel();
          onPrintFail();
          break;

        case 'bleSuccess':
          onConnectSuccess();
          break;

        case 'bleFail':
          onConnectFail();
          break;
        case 'disconnect':
          printTimer?.cancel();
          onDisconnected();
          break;
        default:
      }
    });
  }

  static Future<bool> requestPermissionBT() async {
    _channel.invokeMethod('requestPermissionBT');
    _perBTCompleter = Completer();
    return await _perBTCompleter?.future ?? false;
  }

  static Future<bool> requestPermissionLocation() async {
    _channel.invokeMethod('requestPermissionLocation');
    _perLocationCompleter = Completer();
    return await _perLocationCompleter?.future ?? false;
  }

  static Future<bool> enableBT() async {
    _channel.invokeMethod('enableBT');
    _btCompleter = Completer();
    return await _btCompleter?.future ?? false;
  }

  static Future<bool> enableLocation() async {
    _channel.invokeMethod('enableLocation');
    _locationCompleter = Completer();
    return await _locationCompleter?.future ?? false;
  }

  static bool printerFilter(String bluetoothClass) {
    const printerBTClass = '01100';
    return int.parse(bluetoothClass, radix: 16)
            .toRadixString(2)
            .padLeft(20, '0')
            .split('')
            .reversed
            .join('')
            .substring(8, 13) ==
        printerBTClass;
  }

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<dynamic> get getDataList async {
    return await _channel.invokeMethod('getListBle');
  }

  static Future<dynamic> discoverBTDevices({int timeout = 7}) async {
    log('Checking permission...');
    if (!(await requestPermissionBT()) || !(await requestPermissionLocation()) || !(await enableBT())) {
      return null;
    }
    await enableLocation();
    log('Check permission done.');
    discoverTimer?.cancel();
    discoverTimer = null;
    discoverResult.clear();
    discoverAllResult.clear();
    _discoverCompleter = null;

    var paired = _channel.invokeMethod('getListBle');

    discoverTimer = Timer(
      Duration(seconds: timeout + 1),
      () {
        _discoverCompleter?.complete(discoverResult);
      },
    );

    var args = {'timeout': timeout};
    _channel.invokeMethod('discoverBTDevices', args);
    _discoverCompleter = Completer();
    await _discoverCompleter?.future ?? [];

    log("Paired: ${jsonDecode(await paired)}");
    for (var pair in jsonDecode(await paired)) {
      bool isDiscover = discoverAllResult
          .where(
            (e) => (e['address'] as String).substring(2) == (pair['address'] as String).substring(2),
          )
          .isNotEmpty;

      bool isAdded = discoverResult
          .where(
            (e) => (e['address'] as String) == (pair['address'] as String),
          )
          .isNotEmpty;
      if (printerFilter(pair['btClass'] as String) && isDiscover && !isAdded) {
        discoverResult.add(pair);
      }
    }
    log("$discoverResult");
    return jsonEncode(discoverResult);
  }

  static StreamSubscription listenDicover(
    void Function(dynamic data)? onData, {
    Function? onError,
    void Function()? onDone,
    bool? cancelOnError,
  }) {
    return discoverStream.listen(
      onData,
      onDone: onDone,
      onError: onError,
      cancelOnError: cancelOnError,
    );
  }

  static Future<bool> connectBle(String? address, {bool disconnectCurrentAddress = false}) async {
    log('connectBle: $address-$disconnectCurrentAddress');

    var args = {"address": address, "disconnectCurrentAddress": disconnectCurrentAddress};
    return await _channel.invokeMethod('connectBle', args);
  }

  // static printImages(List<Uint8List> listImage) async {
  //   queue.addAll(listImage);
  //   _printImagesItem();
  // }

  // static _printImagesItem() async {
  //   if (queue.isNotEmpty) {
  //     log('Printing order...');
  //     var args = {"data": await compressImage(queue.first)};
  //     _channel.invokeMethod<bool>('printImage', args);
  //   }
  // }
  static printQrCodeLogin(Map<String, dynamic> json) async {
    _channel.invokeMethod('printQrCodeLogin', json);
  }

  static printOrder(Map<String, dynamic> json) async {
    if (json["form"] == "receiptAndLabBox") {
      var jsonLabBox = jsonDecode(jsonEncode(json));
      jsonLabBox["form"] = "labBox";
      queue.add(jsonLabBox);
      var jsonReceipt = jsonDecode(jsonEncode(json));
      jsonReceipt["form"] = "receipt";
      queue.add(jsonReceipt);
    } else {
      queue.add(json);
    }
    _printOrderItem();
  }

  static _printOrderItem() {
    if (queue.isNotEmpty) {
      _channel.invokeMethod('printOrder', queue.first);
    }
  }

  static Future<Uint8List> compressImage(Uint8List input) async {
    var output = await FlutterImageCompress.compressWithList(
      input,
      quality: 90,
      minWidth: 860,
    );
    return output;
  }
}

Future<File> getImageFileFromAssets(String path) async {
  final byteData = await rootBundle.load('assets/$path');

  final file = File('${(await getTemporaryDirectory()).path}/$path');
  await file.writeAsBytes(byteData.buffer.asUint8List(byteData.offsetInBytes, byteData.lengthInBytes));

  return file;
}

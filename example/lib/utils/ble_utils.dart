import 'dart:io';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:panda_printer_example/utils/extensions/list_extension.dart';
import 'package:permission_handler/permission_handler.dart';

class BleUtils {
  static Future<bool> get isBleOn async {
    return (await Connectivity().checkConnectivity()) == ConnectivityResult.bluetooth;
  }

  static Future<bool> get isBleOff async {
    return !await isBleOn;
  }

  static Future<bool> requestPermissions() async {
    //  bypass for ios, TODO: request permissions on IOS
    if (Platform.isIOS) {
      return true;
    }

    Map<Permission, PermissionStatus> result = await [
      Permission.bluetoothScan,
      Permission.bluetoothConnect,
    ].request();

    return result.values.all(
      (PermissionStatus status) => [PermissionStatus.granted, PermissionStatus.limited].contains(status),
    );
  }
}

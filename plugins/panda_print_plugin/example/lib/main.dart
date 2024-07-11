import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:panda_printer_example/app.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  SystemChrome.setPreferredOrientations([
    DeviceOrientation.landscapeLeft,
    DeviceOrientation.landscapeRight,
  ]);
  runApp(PrinterApp());
}

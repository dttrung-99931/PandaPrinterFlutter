import 'package:flutter/material.dart';
import 'package:panda_printer_example/screens/print_screen.dart';

class PrinterApp extends StatelessWidget {
  const PrinterApp({super.key});
  static final GlobalKey<NavigatorState> _globalKey = GlobalKey();
  static BuildContext get context => _globalKey.currentContext!;
  static NavigatorState get navigator => _globalKey.currentState!;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: _globalKey,
      title: 'Printer app',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const TestPrintScreen(),
    );
  }
}

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:print_plugin/print_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('print_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await PrintPlugin.platformVersion, '42');
  });
}

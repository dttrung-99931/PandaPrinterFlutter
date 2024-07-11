import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:panda_print_plugin/panda_print_plugin_android.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  PandaPrintPluginAndroid platform = PandaPrintPluginAndroid();
  const MethodChannel channel = MethodChannel('panda_print_plugin');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    // expect(await platform.getPlatformVersion(), '42');
  });
}

import 'package:flutter_test/flutter_test.dart';
import 'package:panda_print_plugin/panda_print.dart';
import 'package:panda_print_plugin/panda_print_plugin.dart';
import 'package:panda_print_plugin/panda_print_plugin_android.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockPandaPrintPluginPlatform with MockPlatformInterfaceMixin implements PandaPrintPlugin {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final PandaPrintPlugin initialPlatform = PandaPrintPlugin.instance;

  test('$PandaPrintPluginAndroid is the default instance', () {
    expect(initialPlatform, isInstanceOf<PandaPrintPluginAndroid>());
  });

  test('getPlatformVersion', () async {
    PandaPrint pandaPrintPlugin = PandaPrint();
    MockPandaPrintPluginPlatform fakePlatform = MockPandaPrintPluginPlatform();
    PandaPrintPlugin.instance = fakePlatform;

    // expect(await pandaPrintPlugin.getPlatformVersion(), '42');
  });
}

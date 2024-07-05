// ignore_for_file: invalid_use_of_protected_member

import 'dart:developer';
import 'dart:typed_data';
import 'dart:ui';

import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

extension ContextExt on BuildContext {
  MediaQueryData get mediaQuery => MediaQuery.of(this);
  Size get mediaQuerySize => mediaQuery.size;
  double get width => mediaQuerySize.width;
  double get height => mediaQuerySize.height;
  bool get isKeyboadShowing => MediaQuery.of(this).viewInsets.bottom != 0;
}

extension TextStyleExt on TextStyle {
  TextStyle light() => copyWith(fontWeight: FontWeight.w300);
  TextStyle regular() => copyWith(fontWeight: FontWeight.w400);
  TextStyle bold() => copyWith(fontWeight: FontWeight.bold);

  TextStyle arial() => copyWith(fontFamily: 'Arial');
  TextStyle notoSansJP() => copyWith(fontFamily: 'NotoSansJP');

  TextStyle withColor(Color color) => copyWith(color: color);
  TextStyle spacing(double spacing) => copyWith(letterSpacing: spacing * 0); // spacing from XD
  TextStyle withHeight(double height) => copyWith(height: height / fontSize!); // height from XD
  TextStyle lessOneSize() => copyWith(fontSize: fontSize! - 1);
  TextStyle moreOneSize() => copyWith(fontSize: fontSize! + 1);
}

extension GlobalKeyExt on GlobalKey {
  Offset get position {
    RenderBox? renderBox = (currentContext?.findRenderObject() as RenderBox?);
    return renderBox?.localToGlobal(Offset.zero) ?? Offset.zero;
  }

  /// Capture widget
  ///
  /// Wrap widget with RepaintBoundary and use global key of  RepaintBoundary to call this method to capture image
  Future<Uint8List?> captureWidgetImage({double pixelRatio = 1}) async {
    try {
      RenderObject? renderObj = currentContext?.findRenderObject();
      if (renderObj is! RenderRepaintBoundary) {
        return null;
      }
      RenderRepaintBoundary boundary = renderObj;
      var image = await boundary.toImage(pixelRatio: pixelRatio);
      ByteData? byteData = await image.toByteData(format: ImageByteFormat.png);
      Uint8List? pngBytes = byteData?.buffer.asUint8List();
      return pngBytes;
    } catch (e) {
      log(e.toString());
      return null;
    }
  }
}

extension TextEditingControllerExt on TextEditingController {
  void moveCursorTo(int index) {
    if (index < 0 || index > text.length) {
      return;
    }
    selection = TextSelection.fromPosition(TextPosition(offset: index));
  }
}

extension StateExt on State {
  void setStateIfMounted(VoidCallback action) {
    if (mounted) {
      setState(action);
    }
  }
}

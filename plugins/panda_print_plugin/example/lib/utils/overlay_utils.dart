// ignore_for_file: use_build_context_synchronously

import 'dart:async';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:panda_printer_example/utils/extensions/ui_extensions.dart';
import 'package:panda_printer_example/utils/mixins/disposable_mixin.dart';

class OverlayUtils {
  static String? _showingMsg;
  static final StreamController<Widget> _addSnackWidgetStream = StreamController.broadcast();
  static Stream<Widget> get addSnackWidgetStream => _addSnackWidgetStream.stream;
  static final StreamController<Widget> _removeSnackWidgetStream = StreamController.broadcast();
  static Stream<Widget> get removeSnackWidgetStream => _removeSnackWidgetStream.stream;
  static Duration showingDuration = const Duration(seconds: 3);

  static void resetShowingMessage() {
    _showingMsg = null;
  }

  static void showSnackBar(String msg, [type = SnackType.success]) async {
    // Don't show duplicated snackbar together
    if (_showingMsg == msg) {
      return;
    }
    _showingMsg = msg;
    log('SNACKBAR: $msg');

    late Widget snackMsg;
    snackMsg = CustomFlushbar(
      type: type,
      msg: msg,
      onDismissed: () {
        _showingMsg = null;
      },
      onDragToDismiss: () {
        _removeSnackWidgetStream.add(snackMsg);
      },
    );
    _addSnackWidgetStream.add(snackMsg);
  }
}

class CustomFlushbar extends StatefulWidget {
  const CustomFlushbar({
    super.key,
    required this.type,
    required this.msg,
    required this.onDismissed,
    required this.onDragToDismiss,
  });

  final SnackType type;
  final String msg;
  final Function() onDismissed;
  final Function() onDragToDismiss;

  @override
  State<CustomFlushbar> createState() => _CustomFlushbarState();
}

class _CustomFlushbarState extends State<CustomFlushbar> {
  @override
  void dispose() {
    widget.onDismissed();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onPanEnd: (details) {
        widget.onDragToDismiss();
      },
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 16.0),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: widget.type.color),
        ),
        child: Text(
          widget.msg,
          style: TextStyle(color: widget.type.color),
        ),
      ),
    );
  }
}

enum SnackType {
  success,
  fail,
  normal;

  Color get color {
    Color color;
    switch (this) {
      case SnackType.fail:
        color = Colors.red[400]!;
        break;
      case SnackType.success:
        color = Colors.green[500]!;
        break;
      default:
        color = Colors.black;
    }
    return color;
  }
}

class SnackMessageOverlayWidget extends StatefulWidget {
  const SnackMessageOverlayWidget({
    super.key,
  });

  @override
  State<SnackMessageOverlayWidget> createState() => _SnackMessageOverlayWidgetState();
}

class _SnackMessageOverlayWidgetState extends State<SnackMessageOverlayWidget> with DisposableMixin {
  final List<Widget> showingSnackWidgets = [];

  @override
  void initState() {
    addSubscription(OverlayUtils.addSnackWidgetStream.listen((Widget addedMsg) async {
      showingSnackWidgets.add(addedMsg);
      setStateIfMounted(() {});
      await Future.delayed(OverlayUtils.showingDuration);
      OverlayUtils.resetShowingMessage();
      showingSnackWidgets.remove(addedMsg);
      setStateIfMounted(() {});
    }));

    addSubscription(OverlayUtils.removeSnackWidgetStream.listen((Widget removeMsg) async {
      showingSnackWidgets.remove(removeMsg);
      OverlayUtils.resetShowingMessage();
      setStateIfMounted(() {});
    }));

    super.initState();
  }

  @override
  void dispose() {
    disposeAllSubscriptions();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: showingSnackWidgets,
    );
  }
}

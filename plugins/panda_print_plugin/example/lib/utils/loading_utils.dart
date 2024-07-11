import 'package:flutter/material.dart';
import 'package:panda_printer_example/app.dart';

bool _isLoadingShowing = false;
const String _loadingRoute = '/loading';
void showLoading() {
  if (_isLoadingShowing) {
    return;
  }
  _isLoadingShowing = true;
  PrinterApp.navigator.push(
    PageRouteBuilder(
      barrierColor: Colors.black.withOpacity(0.5),
      opaque: false,
      pageBuilder: (context, animation, secondaryAnimation) {
        return const PopScope(
          canPop: false,
          child: Material(
            type: MaterialType.transparency,
            child: SafeArea(
              child: Center(
                child: SizedBox.square(
                  dimension: 24,
                  child: CircularProgressIndicator(),
                ),
              ),
            ),
          ),
        );
      },
      settings: const RouteSettings(name: _loadingRoute),
      transitionDuration: const Duration(milliseconds: 200),
      reverseTransitionDuration: const Duration(milliseconds: 200),
      transitionsBuilder: (context, animation, secondaryAnimation, child) {
        return AnimatedBuilder(
          animation: animation,
          builder: (context, _) {
            return FadeTransition(
              opacity: animation,
              child: child,
            );
          },
        );
      },
    ),
  );
}

void hideLoading() {
  if (!_isLoadingShowing) {
    return;
  }
  _isLoadingShowing = false;
  PrinterApp.navigator.popUntil(
    (route) => route.settings.name != _loadingRoute,
  );
}

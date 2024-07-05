import 'package:dartz/dartz.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:panda_printer_example/models/app_error.dart';
import 'package:panda_printer_example/utils/loading_utils.dart';

class BaseController extends ChangeNotifier {
  AppError? _error;
  AppError? get error => _error;

  Future<T?> handleServiceResult<T>({
    required Future<Either<AppError, T>> serviceResult,
    required Function(T result) onSuccess,
    Function(AppError error)? onError,
    bool handleLoading = true,
  }) async {
    if (handleLoading) {
      showLoading();
    }
    Either<AppError, T> result = await serviceResult;
    if (handleLoading) {
      hideLoading();
    }
    return result.fold(
      (error) {
        _error = error;
        (onError ?? _onErrorDefault).call(error);
        return null;
      },
      (T result) {
        return result;
      },
    );
  }

  void _onErrorDefault(AppError error) {}

  Future<T> load<T>(Future<T> Function() action) async {
    T result;
    showLoading();
    result = await action();
    hideLoading();
    return result;
  }
}

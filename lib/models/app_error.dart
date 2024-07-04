// ignore_for_file: public_member_api_docs, sort_constructors_first
class AppError {
  final String message;
  final String code;
  AppError({
    required this.message,
    this.code = '',
  });
}

class PrinterError extends AppError {
  PrinterError({required super.message});
}

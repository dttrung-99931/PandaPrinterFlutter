// ignore_for_file: public_member_api_docs, sort_constructors_first
abstract class PrinterError {
  final String message;
  PrinterError({
    required this.message,
  });
}

class ConnectPrinterError extends PrinterError {
  ConnectPrinterError({required super.message});
}

class PrintError extends PrinterError {
  PrintError({required super.message});
}

import 'dart:convert';

// ignore_for_file: public_member_api_docs, sort_constructors_first

enum PrinterManagerStatusCode {
  DISCOVERING,
  DISCOVER_COMPLETE,
  CONNECTING,
  CONNECTED;

  static PrinterManagerStatusCode fromMap(String value) {
    return values.firstWhere((status) => status.name == value);
  }

  String toMap() {
    return name;
  }
}

class PrinterManagerStatus {
  final PrinterManagerStatusCode code;
  final String? message;
  PrinterManagerStatus({
    required this.code,
    this.message,
  });

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'code': code.toMap(),
      'message': message,
    };
  }

  factory PrinterManagerStatus.fromMap(Map<String, dynamic> map) {
    return PrinterManagerStatus(
      code: PrinterManagerStatusCode.fromMap(map['code'] as String),
      message: map['message'] != null ? map['message'] as String : null,
    );
  }

  String toJson() => json.encode(toMap());

  factory PrinterManagerStatus.fromJson(String source) =>
      PrinterManagerStatus.fromMap(json.decode(source) as Map<String, dynamic>);
}

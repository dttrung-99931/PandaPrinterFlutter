import 'dart:convert';

import 'package:panda_print_plugin/models/panda_printer.dart';

// ignore_for_file: public_member_api_docs, sort_constructors_first
class PrinterModel {
  final String address;
  final String? name;
  final String? btClass;
  final bool isBond;
  PrinterModel({
    required this.address,
    required this.name,
    required this.btClass,
    required this.isBond,
  });

  factory PrinterModel.fromPandaPrinter(PandaPrinter printer) {
    return PrinterModel(
      address: printer.address,
      name: printer.name,
      btClass: 'btClass',
      isBond: true,
    );
  }

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'address': address,
      'name': name,
      'btClass': btClass,
      'isBond': isBond,
    };
  }

  factory PrinterModel.fromMap(Map<String, dynamic> map) {
    return PrinterModel(
      address: map['address'] as String,
      name: map['name'] != null ? map['name'] as String : null,
      btClass: map['btClass'] != null ? map['btClass'] as String : null,
      isBond: map['isBond'] as bool,
    );
  }

  String toJson() => json.encode(toMap());

  factory PrinterModel.fromJson(String source) => PrinterModel.fromMap(json.decode(source) as Map<String, dynamic>);
}

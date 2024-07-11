import 'dart:convert';

// ignore_for_file: public_member_api_docs, sort_constructors_first
class PandaPrinter {
  final String name;
  final String address;
  PandaPrinter({
    required this.name,
    required this.address,
  });

  static List<PandaPrinter> fromJsons(String jsonString) {
    List<dynamic> printersJson = jsonDecode(jsonString);
    return printersJson.map((e) => PandaPrinter.fromMap(e)).toList();
  }

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'name': name,
      'address': address,
    };
  }

  factory PandaPrinter.fromMap(Map<String, dynamic> map) {
    return PandaPrinter(
      name: map['name'] as String,
      address: map['address'] as String,
    );
  }

  String toJson() => json.encode(toMap());

  factory PandaPrinter.fromJson(String source) => PandaPrinter.fromMap(json.decode(source) as Map<String, dynamic>);
}

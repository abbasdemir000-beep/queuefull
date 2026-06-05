/// Domain model representing a city.
/// Ported from City in the original Kotlin app.
/// Uses String id (Firestore document ID) instead of Int (Room auto-increment).
class City {
  const City({
    required this.id,
    required this.nameAr,
    required this.nameEn,
    required this.isApproved,
  });

  final String id;
  final String nameAr;
  final String nameEn;
  final bool isApproved;

  City copyWith({
    String? id,
    String? nameAr,
    String? nameEn,
    bool? isApproved,
  }) {
    return City(
      id: id ?? this.id,
      nameAr: nameAr ?? this.nameAr,
      nameEn: nameEn ?? this.nameEn,
      isApproved: isApproved ?? this.isApproved,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'nameAr': nameAr,
      'nameEn': nameEn,
      'isApproved': isApproved,
    };
  }

  factory City.fromJson(Map<String, dynamic> json) {
    return City(
      id: json['id'] as String? ?? '',
      nameAr: json['nameAr'] as String? ?? '',
      nameEn: json['nameEn'] as String? ?? '',
      isApproved: json['isApproved'] as bool? ?? false,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is City && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;

  @override
  String toString() => 'City(id: $id, nameEn: $nameEn)';
}

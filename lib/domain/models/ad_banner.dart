/// Domain model representing an advertisement banner.
/// Ported from AdBanner in the original Kotlin app.
/// Uses String id (Firestore document ID) instead of Int (Room auto-increment).
class AdBanner {
  const AdBanner({
    required this.id,
    required this.title,
    required this.description,
    required this.city,
    required this.category,
    this.ctaPhone,
    this.imageUrl,
  });

  final String id;
  final String title;
  final String description;
  final String city;
  final String category;
  final String? ctaPhone;
  final String? imageUrl;

  AdBanner copyWith({
    String? id,
    String? title,
    String? description,
    String? city,
    String? category,
    String? ctaPhone,
    String? imageUrl,
  }) {
    return AdBanner(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      city: city ?? this.city,
      category: category ?? this.category,
      ctaPhone: ctaPhone ?? this.ctaPhone,
      imageUrl: imageUrl ?? this.imageUrl,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'city': city,
      'category': category,
      'ctaPhone': ctaPhone,
      'imageUrl': imageUrl,
    };
  }

  factory AdBanner.fromJson(Map<String, dynamic> json) {
    return AdBanner(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      description: json['description'] as String? ?? '',
      city: json['city'] as String? ?? '',
      category: json['category'] as String? ?? '',
      ctaPhone: json['ctaPhone'] as String?,
      imageUrl: json['imageUrl'] as String?,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is AdBanner && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;

  @override
  String toString() => 'AdBanner(id: $id, title: $title)';
}

import '../../core/constants/app_constants.dart';

/// Domain model representing a fuel station.
/// Ported from Station in the original Kotlin app.
/// Uses String id (Firestore document ID) instead of Int (Room auto-increment).
class Station {
  const Station({
    required this.id,
    required this.cityId,
    required this.cityName,
    required this.name,
    required this.latitude,
    required this.longitude,
    required this.type,
    required this.fuelTypes,
    required this.queueStatus,
    required this.lastUpdated,
    required this.confirmedCount,
    required this.hasFuel,
    required this.isApproved,
    this.suggestedBy,
  });

  final String id;
  final String cityId;
  final String cityName;
  final String name;
  final double latitude;
  final double longitude;
  final StationType type;
  final List<FuelType> fuelTypes;
  final QueueStatus queueStatus;
  final DateTime lastUpdated;
  final int confirmedCount;
  final bool hasFuel;
  final bool isApproved;
  final String? suggestedBy;

  Station copyWith({
    String? id,
    String? cityId,
    String? cityName,
    String? name,
    double? latitude,
    double? longitude,
    StationType? type,
    List<FuelType>? fuelTypes,
    QueueStatus? queueStatus,
    DateTime? lastUpdated,
    int? confirmedCount,
    bool? hasFuel,
    bool? isApproved,
    String? suggestedBy,
  }) {
    return Station(
      id: id ?? this.id,
      cityId: cityId ?? this.cityId,
      cityName: cityName ?? this.cityName,
      name: name ?? this.name,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      type: type ?? this.type,
      fuelTypes: fuelTypes ?? this.fuelTypes,
      queueStatus: queueStatus ?? this.queueStatus,
      lastUpdated: lastUpdated ?? this.lastUpdated,
      confirmedCount: confirmedCount ?? this.confirmedCount,
      hasFuel: hasFuel ?? this.hasFuel,
      isApproved: isApproved ?? this.isApproved,
      suggestedBy: suggestedBy ?? this.suggestedBy,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'cityId': cityId,
      'cityName': cityName,
      'name': name,
      'latitude': latitude,
      'longitude': longitude,
      'type': type.toFirestore,
      'fuelTypes': fuelTypes.map((e) => e.toFirestore).toList(),
      'queueStatus': queueStatus.toFirestore,
      'lastUpdated': lastUpdated.millisecondsSinceEpoch,
      'confirmedCount': confirmedCount,
      'hasFuel': hasFuel,
      'isApproved': isApproved,
      'suggestedBy': suggestedBy,
    };
  }

  factory Station.fromJson(Map<String, dynamic> json) {
    return Station(
      id: json['id'] as String? ?? '',
      cityId: json['cityId'] as String? ?? '',
      cityName: json['cityName'] as String? ?? '',
      name: json['name'] as String? ?? '',
      latitude: (json['latitude'] as num?)?.toDouble() ?? 0.0,
      longitude: (json['longitude'] as num?)?.toDouble() ?? 0.0,
      type: StationType.fromFirestore(json['type'] as String? ?? 'GOVERNMENT'),
      fuelTypes: (json['fuelTypes'] as List<dynamic>?)
              ?.map((e) => FuelType.fromFirestore(e as String))
              .toList() ??
          [],
      queueStatus:
          QueueStatus.fromFirestore(json['queueStatus'] as String? ?? 'EMPTY'),
      lastUpdated: json['lastUpdated'] != null
          ? DateTime.fromMillisecondsSinceEpoch(json['lastUpdated'] as int)
          : DateTime.now(),
      confirmedCount: json['confirmedCount'] as int? ?? 0,
      hasFuel: json['hasFuel'] as bool? ?? false,
      isApproved: json['isApproved'] as bool? ?? false,
      suggestedBy: json['suggestedBy'] as String?,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is Station && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;

  @override
  String toString() => 'Station(id: $id, name: $name, status: ${queueStatus.toFirestore})';
}

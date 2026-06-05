import '../../core/constants/app_constants.dart';

/// Domain model representing a queue status update submitted by a user.
/// Ported from QueueUpdate in the original Kotlin app.
/// Uses String id (Firestore document ID) instead of Int (Room auto-increment).
class QueueUpdate {
  const QueueUpdate({
    required this.id,
    required this.stationId,
    required this.queueStatus,
    required this.hasFuel,
    required this.fuelType,
    required this.timestamp,
    required this.userPhone,
    required this.latitude,
    required this.longitude,
  });

  final String id;
  final String stationId;
  final QueueStatus queueStatus;
  final bool hasFuel;
  final FuelType fuelType;
  final DateTime timestamp;
  final String userPhone;
  final double latitude;
  final double longitude;

  QueueUpdate copyWith({
    String? id,
    String? stationId,
    QueueStatus? queueStatus,
    bool? hasFuel,
    FuelType? fuelType,
    DateTime? timestamp,
    String? userPhone,
    double? latitude,
    double? longitude,
  }) {
    return QueueUpdate(
      id: id ?? this.id,
      stationId: stationId ?? this.stationId,
      queueStatus: queueStatus ?? this.queueStatus,
      hasFuel: hasFuel ?? this.hasFuel,
      fuelType: fuelType ?? this.fuelType,
      timestamp: timestamp ?? this.timestamp,
      userPhone: userPhone ?? this.userPhone,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'stationId': stationId,
      'queueStatus': queueStatus.toFirestore,
      'hasFuel': hasFuel,
      'fuelType': fuelType.toFirestore,
      'timestamp': timestamp.millisecondsSinceEpoch,
      'userPhone': userPhone,
      'latitude': latitude,
      'longitude': longitude,
    };
  }

  factory QueueUpdate.fromJson(Map<String, dynamic> json) {
    return QueueUpdate(
      id: json['id'] as String? ?? '',
      stationId: json['stationId'] as String? ?? '',
      queueStatus:
          QueueStatus.fromFirestore(json['queueStatus'] as String? ?? 'EMPTY'),
      hasFuel: json['hasFuel'] as bool? ?? false,
      fuelType:
          FuelType.fromFirestore(json['fuelType'] as String? ?? 'REGULAR'),
      timestamp: json['timestamp'] != null
          ? DateTime.fromMillisecondsSinceEpoch(json['timestamp'] as int)
          : DateTime.now(),
      userPhone: json['userPhone'] as String? ?? '',
      latitude: (json['latitude'] as num?)?.toDouble() ?? 0.0,
      longitude: (json['longitude'] as num?)?.toDouble() ?? 0.0,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is QueueUpdate && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;

  @override
  String toString() =>
      'QueueUpdate(id: $id, stationId: $stationId, status: ${queueStatus.toFirestore})';
}

import '../../core/constants/app_constants.dart';

/// Domain model representing an app user.
/// Ported from AppUser in the original Kotlin app.
/// Uses String uid (Firestore document ID) instead of Int (Room auto-increment).
class AppUser {
  const AppUser({
    required this.uid,
    required this.phoneNumber,
    required this.name,
    required this.role,
    required this.points,
    required this.banned,
    required this.createdAt,
  });

  final String uid;
  final String phoneNumber;
  final String name;
  final UserRole role;
  final int points;
  final bool banned;
  final DateTime createdAt;

  AppUser copyWith({
    String? uid,
    String? phoneNumber,
    String? name,
    UserRole? role,
    int? points,
    bool? banned,
    DateTime? createdAt,
  }) {
    return AppUser(
      uid: uid ?? this.uid,
      phoneNumber: phoneNumber ?? this.phoneNumber,
      name: name ?? this.name,
      role: role ?? this.role,
      points: points ?? this.points,
      banned: banned ?? this.banned,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'uid': uid,
      'phoneNumber': phoneNumber,
      'name': name,
      'role': role.toFirestore,
      'points': points,
      'banned': banned,
      'createdAt': createdAt.millisecondsSinceEpoch,
    };
  }

  factory AppUser.fromJson(Map<String, dynamic> json) {
    return AppUser(
      uid: json['uid'] as String? ?? '',
      phoneNumber: json['phoneNumber'] as String? ?? '',
      name: json['name'] as String? ?? '',
      role: UserRole.fromFirestore(json['role'] as String? ?? 'USER'),
      points: json['points'] as int? ?? 0,
      banned: json['banned'] as bool? ?? false,
      createdAt: json['createdAt'] != null
          ? DateTime.fromMillisecondsSinceEpoch(json['createdAt'] as int)
          : DateTime.now(),
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is AppUser && other.uid == uid;
  }

  @override
  int get hashCode => uid.hashCode;

  @override
  String toString() => 'AppUser(uid: $uid, name: $name, role: ${role.toFirestore})';
}

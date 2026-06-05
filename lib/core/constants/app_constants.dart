/// Queue status values matching the original Kotlin app
enum QueueStatus {
  empty,
  short_,
  moderate,
  long_,
  closed;

  String get displayName {
    switch (this) {
      case QueueStatus.empty:
        return '\u0641\u0627\u0631\u063A';
      case QueueStatus.short_:
        return '\u0642\u0635\u064A\u0631';
      case QueueStatus.moderate:
        return '\u0645\u062A\u0648\u0633\u0637';
      case QueueStatus.long_:
        return '\u0637\u0648\u064A\u0644';
      case QueueStatus.closed:
        return '\u0645\u063A\u0644\u0642';
    }
  }

  String get toFirestore {
    switch (this) {
      case QueueStatus.empty:
        return 'EMPTY';
      case QueueStatus.short_:
        return 'SHORT';
      case QueueStatus.moderate:
        return 'MODERATE';
      case QueueStatus.long_:
        return 'LONG';
      case QueueStatus.closed:
        return 'CLOSED';
    }
  }

  static QueueStatus fromFirestore(String value) {
    switch (value.toUpperCase()) {
      case 'EMPTY':
        return QueueStatus.empty;
      case 'SHORT':
        return QueueStatus.short_;
      case 'MODERATE':
        return QueueStatus.moderate;
      case 'LONG':
        return QueueStatus.long_;
      case 'CLOSED':
        return QueueStatus.closed;
      default:
        return QueueStatus.empty;
    }
  }
}

/// User roles matching the original Kotlin app
enum UserRole {
  user,
  reporter,
  admin;

  String get displayName {
    switch (this) {
      case UserRole.user:
        return '\u0645\u0633\u062A\u062E\u062F\u0645';
      case UserRole.reporter:
        return '\u0645\u0631\u0627\u0633\u0644';
      case UserRole.admin:
        return '\u0645\u0633\u0624\u0648\u0644';
    }
  }

  String get toFirestore {
    switch (this) {
      case UserRole.user:
        return 'USER';
      case UserRole.reporter:
        return 'REPORTER';
      case UserRole.admin:
        return 'ADMIN';
    }
  }

  static UserRole fromFirestore(String value) {
    switch (value.toUpperCase()) {
      case 'USER':
        return UserRole.user;
      case 'REPORTER':
        return UserRole.reporter;
      case 'ADMIN':
        return UserRole.admin;
      default:
        return UserRole.user;
    }
  }
}

/// Fuel type enum
enum FuelType {
  regular,
  improved,
  super_;

  String get displayName {
    switch (this) {
      case FuelType.regular:
        return '\u0639\u0627\u062F\u064A';
      case FuelType.improved:
        return '\u0645\u062D\u0633\u0651\u0646';
      case FuelType.super_:
        return '\u0633\u0648\u0628\u0631';
    }
  }

  String get toFirestore {
    switch (this) {
      case FuelType.regular:
        return 'REGULAR';
      case FuelType.improved:
        return 'IMPROVED';
      case FuelType.super_:
        return 'SUPER';
    }
  }

  static FuelType fromFirestore(String value) {
    switch (value.toUpperCase()) {
      case 'REGULAR':
        return FuelType.regular;
      case 'IMPROVED':
        return FuelType.improved;
      case 'SUPER':
        return FuelType.super_;
      default:
        return FuelType.regular;
    }
  }
}

/// Station type enum
enum StationType {
  government,
  private_;

  String get displayName {
    switch (this) {
      case StationType.government:
        return '\u062D\u0643\u0648\u0645\u064A';
      case StationType.private_:
        return '\u0623\u0647\u0644\u064A';
    }
  }

  String get toFirestore {
    switch (this) {
      case StationType.government:
        return 'GOVERNMENT';
      case StationType.private_:
        return 'PRIVATE';
    }
  }

  static StationType fromFirestore(String value) {
    switch (value.toUpperCase()) {
      case 'GOVERNMENT':
        return StationType.government;
      case 'PRIVATE':
        return StationType.private_;
      default:
        return StationType.government;
    }
  }
}

/// Application-wide constants ported from the original Kotlin app
class AppConstants {
  AppConstants._();

  // Admin
  static const String adminPhone = '07774564334';

  // Supported Cities
  static const List<String> supportedCities = [
    'Kirkuk',
    'Erbil',
    'Sulaymaniyah',
  ];

  // GPS
  static const double gpsRadiusMeters = 200.0;
  static const double earthRadiusMeters = 6371000.0;

  // Cycle
  static const int cycleDurationMs = 18000000; // 5 hours
  static const int preEndNoticeMs = 17880000; // 4h58m
  static const Duration cycleDuration = Duration(milliseconds: cycleDurationMs);

  // Status Expiry
  static const int statusExpiryThresholdMs = 2400000; // 40 minutes
  static const Duration statusExpiryThreshold =
      Duration(milliseconds: statusExpiryThresholdMs);
  static const String expiredStatus = 'EMPTY';

  // Points
  static const int reportPoints = 15;
  static const int confirmationPoints = 5;
  static const int stationSuggestionPoints = 30;
  static const int stationApprovalPoints = 50;
  static const int welcomePoints = 20;
  static const int cycleResetPoints = 20;
  static const int adminInitialPoints = 250;

  // Auth
  static const int minPhoneLength = 10;

  // Firebase
  static const String firebaseProjectId = 'queue-6a58d';

  // Firestore Collections
  static const String stationsCollection = 'stations';
  static const String citiesCollection = 'cities';
  static const String usersCollection = 'users';
  static const String queueUpdatesCollection = 'queue_updates';
  static const String notificationsCollection = 'notifications';
  static const String adBannersCollection = 'ad_banners';
}

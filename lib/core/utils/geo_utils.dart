import 'dart:math';

/// Utility class for geographic distance calculations.
/// Ported from GeoProximity.kt in the original Kotlin app.
class GeoUtils {
  GeoUtils._();

  /// Default radius threshold in meters for proximity checks
  static const double defaultRadiusMeters = 200.0;

  /// Earth radius in meters for Haversine calculation
  static const double earthRadiusMeters = 6371000.0;

  /// Calculate the distance in meters between two geographic coordinates
  /// using the Haversine formula.
  static double distanceMeters(
    double lat1,
    double lon1,
    double lat2,
    double lon2,
  ) {
    final double dLat = _toRadians(lat2 - lat1);
    final double dLon = _toRadians(lon2 - lon1);

    final double a = sin(dLat / 2) * sin(dLat / 2) +
        cos(_toRadians(lat1)) *
            cos(_toRadians(lat2)) *
            sin(dLon / 2) *
            sin(dLon / 2);

    final double c = 2 * atan2(sqrt(a), sqrt(1 - a));

    return earthRadiusMeters * c;
  }

  /// Check if two coordinates are within the given radius (default 200m).
  static bool isWithinRadius(
    double lat1,
    double lon1,
    double lat2,
    double lon2, {
    double radius = defaultRadiusMeters,
  }) {
    return distanceMeters(lat1, lon1, lat2, lon2) <= radius;
  }

  static double _toRadians(double degrees) {
    return degrees * pi / 180.0;
  }
}

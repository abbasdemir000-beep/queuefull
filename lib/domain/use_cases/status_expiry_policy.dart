/// Status expiry policy ported from StatusExpiryPolicy.kt.
/// Determines when a station's reported queue status is considered stale.
class StatusExpiryPolicy {
  StatusExpiryPolicy._();

  /// Expiry threshold in milliseconds (40 minutes)
  static const int expiryThresholdMs = 2400000;

  /// The status value assigned when a report has expired
  static const String expiredStatus = 'EMPTY';

  /// Expiry threshold as a Dart Duration
  static const Duration expiryThreshold =
      Duration(milliseconds: expiryThresholdMs);

  /// Check if a status update has expired based on its timestamp.
  /// Returns true if the elapsed time since [lastUpdated] exceeds
  /// [expiryThresholdMs] (40 minutes).
  static bool isExpired(DateTime lastUpdated) {
    final elapsed = DateTime.now().difference(lastUpdated);
    return elapsed.inMilliseconds >= expiryThresholdMs;
  }
}

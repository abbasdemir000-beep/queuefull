/// Utility class for cycle timing and status expiry logic.
/// Ported from CyclePolicy.kt timing helpers.
class AppDateUtils {
  AppDateUtils._();

  /// Cycle duration: 5 hours
  static const Duration cycleDuration = Duration(hours: 5);

  /// Status expiry threshold: 40 minutes
  static const Duration expiryThreshold = Duration(minutes: 40);

  /// Format remaining milliseconds as "hh:mm:ss"
  static String formatRemaining(int remainingMs) {
    if (remainingMs <= 0) return '00:00:00';

    final int totalSeconds = remainingMs ~/ 1000;
    final int hours = totalSeconds ~/ 3600;
    final int minutes = (totalSeconds % 3600) ~/ 60;
    final int seconds = totalSeconds % 60;

    return '${hours.toString().padLeft(2, '0')}:'
        '${minutes.toString().padLeft(2, '0')}:'
        '${seconds.toString().padLeft(2, '0')}';
  }

  /// Check if a timestamp is expired based on the given threshold
  /// (default: 40 minutes).
  static bool isExpired(
    DateTime lastUpdated, {
    Duration threshold = const Duration(minutes: 40),
  }) {
    final elapsed = DateTime.now().difference(lastUpdated);
    return elapsed >= threshold;
  }
}

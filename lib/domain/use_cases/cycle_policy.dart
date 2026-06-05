/// Cycle policy ported from CyclePolicy.kt.
/// Manages the 5-hour reporting cycle used for leaderboard resets.
class CyclePolicy {
  CyclePolicy._();

  /// Cycle duration in milliseconds (5 hours)
  static const int cycleDurationMs = 18000000;

  /// Pre-end notice delay in milliseconds (4 hours 58 minutes)
  static const int preEndNoticeDelayMs = 17880000;

  /// Cycle duration as a Dart Duration
  static const Duration cycleDuration = Duration(milliseconds: cycleDurationMs);

  /// Pre-end notice delay as a Dart Duration
  static const Duration preEndNoticeDelay =
      Duration(milliseconds: preEndNoticeDelayMs);

  /// Calculate remaining milliseconds in the current cycle.
  /// [cycleStartTime] is the start of the current cycle in milliseconds
  /// since epoch.
  static int remainingMs(int cycleStartTime) {
    final now = DateTime.now().millisecondsSinceEpoch;
    final elapsed = now - cycleStartTime;
    final remaining = cycleDurationMs - elapsed;
    return remaining > 0 ? remaining : 0;
  }

  /// Check if the current cycle is complete.
  static bool isCycleComplete(int cycleStartTime) {
    return remainingMs(cycleStartTime) <= 0;
  }

  /// Check if the pre-end notification should be sent.
  /// This is true when elapsed time >= preEndNoticeDelayMs (4h58m)
  /// but the cycle is not yet complete.
  static bool shouldSendPreEndNotice(int cycleStartTime) {
    final now = DateTime.now().millisecondsSinceEpoch;
    final elapsed = now - cycleStartTime;
    return elapsed >= preEndNoticeDelayMs && elapsed < cycleDurationMs;
  }

  /// Format remaining time as "hh:mm:ss".
  static String formatRemaining(int cycleStartTime) {
    final remaining = remainingMs(cycleStartTime);
    if (remaining <= 0) return '00:00:00';

    final totalSeconds = remaining ~/ 1000;
    final hours = totalSeconds ~/ 3600;
    final minutes = (totalSeconds % 3600) ~/ 60;
    final seconds = totalSeconds % 60;

    return '${hours.toString().padLeft(2, '0')}:'
        '${minutes.toString().padLeft(2, '0')}:'
        '${seconds.toString().padLeft(2, '0')}';
  }
}

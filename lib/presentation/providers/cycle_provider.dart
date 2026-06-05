import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/use_cases/cycle_policy.dart';

/// State representing the current cycle status.
class CycleState {
  const CycleState({
    required this.cycleStartTime,
    required this.remainingMs,
    required this.formattedTime,
    required this.isComplete,
  });

  final DateTime cycleStartTime;
  final int remainingMs;
  final String formattedTime;
  final bool isComplete;

  factory CycleState.initial() {
    final now = DateTime.now();
    final startMs = now.millisecondsSinceEpoch;
    final remaining = CyclePolicy.remainingMs(startMs);
    return CycleState(
      cycleStartTime: now,
      remainingMs: remaining,
      formattedTime: CyclePolicy.formatRemaining(startMs),
      isComplete: remaining <= 0,
    );
  }
}

/// StateNotifier managing the cycle timer.
/// Starts a periodic timer (1 second interval) that updates the remaining time
/// and detects cycle completion.
class CycleNotifier extends StateNotifier<CycleState> {
  CycleNotifier() : super(CycleState.initial()) {
    _startTimer();
  }

  Timer? _timer;

  void _startTimer() {
    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      _tick();
    });
  }

  void _tick() {
    final startMs = state.cycleStartTime.millisecondsSinceEpoch;
    final remaining = CyclePolicy.remainingMs(startMs);
    final formatted = CyclePolicy.formatRemaining(startMs);
    final isComplete = remaining <= 0;

    state = CycleState(
      cycleStartTime: state.cycleStartTime,
      remainingMs: remaining,
      formattedTime: formatted,
      isComplete: isComplete,
    );

    // If cycle is complete, start a new cycle
    if (isComplete) {
      _startNewCycle();
    }
  }

  /// Start a new cycle with the current time as the start.
  void _startNewCycle() {
    final now = DateTime.now();
    final startMs = now.millisecondsSinceEpoch;
    state = CycleState(
      cycleStartTime: now,
      remainingMs: CyclePolicy.cycleDurationMs,
      formattedTime: CyclePolicy.formatRemaining(startMs),
      isComplete: false,
    );
  }

  /// Manually set the cycle start time (e.g., from server sync).
  void setCycleStart(DateTime startTime) {
    final startMs = startTime.millisecondsSinceEpoch;
    final remaining = CyclePolicy.remainingMs(startMs);
    state = CycleState(
      cycleStartTime: startTime,
      remainingMs: remaining,
      formattedTime: CyclePolicy.formatRemaining(startMs),
      isComplete: remaining <= 0,
    );
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}

/// Provider for the cycle timer state.
final cycleProvider = StateNotifierProvider<CycleNotifier, CycleState>(
  (ref) => CycleNotifier(),
);

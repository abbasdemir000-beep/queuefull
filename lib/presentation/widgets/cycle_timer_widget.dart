import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/cycle_provider.dart';

/// Widget that displays the remaining cycle time with countdown.
class CycleTimerWidget extends ConsumerWidget {
  const CycleTimerWidget({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cycleState = ref.watch(cycleProvider);
    final isEndingSoon = cycleState.remainingMs < 120000; // < 2 minutes

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      decoration: BoxDecoration(
        color: isEndingSoon
            ? Colors.red.withOpacity(0.1)
            : Colors.teal.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isEndingSoon ? Colors.red : Colors.teal,
          width: 1.5,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.timer,
            color: isEndingSoon ? Colors.red : Colors.teal,
            size: 20,
          ),
          const SizedBox(width: 8),
          if (isEndingSoon)
            _PulsingText(
              text: cycleState.formattedTime,
              color: Colors.red,
            )
          else
            Text(
              cycleState.formattedTime,
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                fontFamily: 'monospace',
                color: Colors.teal,
              ),
            ),
          if (isEndingSoon) ...[
            const SizedBox(width: 8),
            const Text(
              '\u0627\u0644\u062F\u0648\u0631\u0629 \u0639\u0644\u0649 \u0648\u0634\u0643 \u0627\u0644\u0627\u0646\u062A\u0647\u0627\u0621!',
              style: TextStyle(
                fontSize: 12,
                color: Colors.red,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

/// Animated pulsing text for cycle ending warning.
class _PulsingText extends StatefulWidget {
  const _PulsingText({required this.text, required this.color});

  final String text;
  final Color color;

  @override
  State<_PulsingText> createState() => _PulsingTextState();
}

class _PulsingTextState extends State<_PulsingText>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    )..repeat(reverse: true);
    _animation = Tween<double>(begin: 0.5, end: 1.0).animate(_controller);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return Opacity(
          opacity: _animation.value,
          child: Text(
            widget.text,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              fontFamily: 'monospace',
              color: widget.color,
            ),
          ),
        );
      },
    );
  }
}

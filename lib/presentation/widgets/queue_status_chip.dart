import 'package:flutter/material.dart';

import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';

/// Reusable colored chip widget for displaying queue status.
class QueueStatusChip extends StatelessWidget {
  const QueueStatusChip({
    super.key,
    required this.status,
    this.large = false,
    this.onTap,
    this.selected = false,
  });

  final QueueStatus status;
  final bool large;
  final VoidCallback? onTap;
  final bool selected;

  Color get _backgroundColor {
    switch (status) {
      case QueueStatus.empty:
        return AppColors.statusEmpty;
      case QueueStatus.short_:
        return AppColors.statusShort;
      case QueueStatus.moderate:
        return AppColors.statusModerate;
      case QueueStatus.long_:
        return AppColors.statusLong;
      case QueueStatus.closed:
        return AppColors.statusClosed;
    }
  }

  Color get _textColor {
    switch (status) {
      case QueueStatus.short_:
        return Colors.black87;
      default:
        return Colors.white;
    }
  }

  @override
  Widget build(BuildContext context) {
    final chip = Chip(
      label: Text(
        status.displayName,
        style: TextStyle(
          color: _textColor,
          fontSize: large ? 16 : 12,
          fontWeight: large ? FontWeight.bold : FontWeight.normal,
        ),
      ),
      backgroundColor: _backgroundColor,
      padding: large
          ? const EdgeInsets.symmetric(horizontal: 12, vertical: 8)
          : const EdgeInsets.symmetric(horizontal: 4, vertical: 0),
      side: selected
          ? const BorderSide(color: Colors.black, width: 2)
          : BorderSide.none,
    );

    if (onTap != null) {
      return GestureDetector(
        onTap: onTap,
        child: chip,
      );
    }

    return chip;
  }
}

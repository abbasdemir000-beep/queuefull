import 'package:flutter/material.dart';

/// Semantic colors for the QueueFuel app
class AppColors {
  AppColors._();

  // Brand
  static const Color primary = Color(0xFF00897B); // Teal 600
  static const Color primaryDark = Color(0xFF00695C); // Teal 800
  static const Color primaryLight = Color(0xFF4DB6AC); // Teal 300
  static const Color secondary = Color(0xFF1565C0); // Blue 800

  // Queue status colors
  static const Color statusEmpty = Color(0xFF4CAF50); // Green
  static const Color statusShort = Color(0xFFFFEB3B); // Yellow
  static const Color statusModerate = Color(0xFFFF9800); // Orange
  static const Color statusLong = Color(0xFFF44336); // Red
  static const Color statusClosed = Color(0xFF9E9E9E); // Grey

  // Light theme
  static const Color lightBackground = Color(0xFFF5F5F5);
  static const Color lightSurface = Color(0xFFFFFFFF);
  static const Color lightOnBackground = Color(0xFF212121);
  static const Color lightOnSurface = Color(0xFF212121);
  static const Color lightError = Color(0xFFD32F2F);
  static const Color lightOnError = Color(0xFFFFFFFF);

  // Dark theme
  static const Color darkBackground = Color(0xFF121212);
  static const Color darkSurface = Color(0xFF1E1E1E);
  static const Color darkOnBackground = Color(0xFFE0E0E0);
  static const Color darkOnSurface = Color(0xFFE0E0E0);
  static const Color darkError = Color(0xFFEF5350);
  static const Color darkOnError = Color(0xFF000000);

  /// Returns the color associated with a queue status string
  static Color colorForStatus(String status) {
    switch (status.toUpperCase()) {
      case 'EMPTY':
        return statusEmpty;
      case 'SHORT':
        return statusShort;
      case 'MODERATE':
        return statusModerate;
      case 'LONG':
        return statusLong;
      case 'CLOSED':
        return statusClosed;
      default:
        return statusEmpty;
    }
  }
}

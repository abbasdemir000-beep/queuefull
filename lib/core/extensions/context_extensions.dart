import 'package:flutter/material.dart';

/// Extension methods on BuildContext for quick access to theme and utilities
extension ContextExtensions on BuildContext {
  /// Get the current ThemeData
  ThemeData get theme => Theme.of(this);

  /// Get the current TextTheme
  TextTheme get textTheme => Theme.of(this).textTheme;

  /// Get the current ColorScheme
  ColorScheme get colorScheme => Theme.of(this).colorScheme;

  /// Get the screen size
  Size get screenSize => MediaQuery.sizeOf(this);

  /// Show a SnackBar with a message
  void showSnackBar(String message) {
    ScaffoldMessenger.of(this).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }
}

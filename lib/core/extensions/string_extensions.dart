/// Extension methods on String for common formatting
extension StringExtensions on String {
  /// Format an Iraqi phone number for display.
  /// Adds spaces: 07XX XXX XXXX
  String get phoneFormatted {
    final cleaned = replaceAll(RegExp(r'\s+'), '');
    if (cleaned.length < 10) return this;
    // Format as: 07XX XXX XXXX
    return '${cleaned.substring(0, 4)} '
        '${cleaned.substring(4, 7)} '
        '${cleaned.substring(7)}';
  }

  /// Capitalize the first character of the string
  String get capitalizeFirst {
    if (isEmpty) return this;
    return '${this[0].toUpperCase()}${substring(1)}';
  }
}

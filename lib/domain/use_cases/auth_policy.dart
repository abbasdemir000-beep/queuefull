import '../../core/constants/app_constants.dart';
import '../models/app_user.dart';

/// Auth policy ported from AuthPolicy.kt.
/// Handles phone validation, role resolution, and ban checks.
/// Backdoor OTP removed - uses real Firebase Auth exclusively.
class AuthPolicy {
  AuthPolicy._();

  /// The admin phone number
  static const String adminPhone = '07774564334';

  /// Minimum length for a valid phone number
  static const int minPhoneLength = 10;

  /// Validate that a phone number meets minimum length requirements.
  static bool isValidPhone(String phone) {
    final cleaned = phone.replaceAll(RegExp(r'\s+'), '');
    return cleaned.length >= minPhoneLength;
  }

  /// Validate that a user name is non-empty.
  static bool isValidName(String name) {
    return name.trim().isNotEmpty;
  }

  /// Resolve the role for a given phone number.
  /// Returns [UserRole.admin] if the phone matches the admin phone,
  /// otherwise returns [UserRole.user].
  static UserRole resolveRole(String phoneNumber) {
    final cleaned = phoneNumber.replaceAll(RegExp(r'\s+'), '');
    if (isAdminPhone(cleaned)) {
      return UserRole.admin;
    }
    return UserRole.user;
  }

  /// Check if a phone number is the admin phone.
  static bool isAdminPhone(String phoneNumber) {
    final cleaned = phoneNumber.replaceAll(RegExp(r'\s+'), '');
    return cleaned == adminPhone || cleaned.endsWith(adminPhone);
  }

  /// Check if a user is banned.
  static bool isUserBanned(AppUser? user) {
    return user?.banned ?? false;
  }
}

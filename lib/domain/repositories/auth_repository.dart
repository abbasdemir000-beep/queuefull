import '../models/app_user.dart';

/// Abstract repository defining the authentication contract.
/// Implementation will use Firebase Auth.
abstract class AuthRepository {
  /// Send OTP to the given phone number.
  /// Returns a verification ID to be used with [verifyOtp].
  Future<String> sendOtp(String phoneNumber);

  /// Verify the OTP code against the verification ID.
  /// Returns the authenticated [AppUser].
  Future<AppUser> verifyOtp(String verificationId, String otp);

  /// Get the currently authenticated user, or null if not signed in.
  Future<AppUser?> getCurrentUser();

  /// Sign out the current user.
  Future<void> signOut();

  /// Delete the current user's account.
  Future<void> deleteAccount();

  /// Stream of auth state changes (user or null).
  Stream<AppUser?> authStateChanges();
}

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/repositories/firebase_auth_repository.dart';
import '../../domain/models/app_user.dart';
import 'repository_providers.dart';

/// Stream provider watching Firebase auth state changes.
/// Emits the current [AppUser] or null when not authenticated.
final authStateProvider = StreamProvider<AppUser?>((ref) {
  final authRepo = ref.watch(authRepositoryProvider);
  return authRepo.authStateChanges();
});

/// Phone verification flow states.
enum PhoneVerificationStatus {
  idle,
  sendingOtp,
  codeSent,
  verifying,
  verified,
  error,
}

/// State for the phone verification flow.
class PhoneVerificationState {
  const PhoneVerificationState({
    this.status = PhoneVerificationStatus.idle,
    this.verificationId,
    this.user,
    this.errorMessage,
  });

  final PhoneVerificationStatus status;
  final String? verificationId;
  final AppUser? user;
  final String? errorMessage;

  PhoneVerificationState copyWith({
    PhoneVerificationStatus? status,
    String? verificationId,
    AppUser? user,
    String? errorMessage,
  }) {
    return PhoneVerificationState(
      status: status ?? this.status,
      verificationId: verificationId ?? this.verificationId,
      user: user ?? this.user,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }
}

/// StateNotifier managing the phone verification flow.
class PhoneVerificationNotifier extends StateNotifier<PhoneVerificationState> {
  PhoneVerificationNotifier(this._ref)
      : super(const PhoneVerificationState());

  final Ref _ref;

  /// Send OTP to the provided phone number.
  Future<void> sendOtp(String phoneNumber) async {
    state = state.copyWith(status: PhoneVerificationStatus.sendingOtp);

    try {
      final authRepo = _ref.read(authRepositoryProvider);
      final verificationId = await authRepo.sendOtp(phoneNumber);

      // Check if Android auto-verified the SMS
      if (verificationId == kAutoVerifiedSentinel) {
        // User is already signed in - fetch current user and transition to verified
        final user = await authRepo.getCurrentUser();
        state = PhoneVerificationState(
          status: PhoneVerificationStatus.verified,
          user: user,
        );
      } else {
        state = state.copyWith(
          status: PhoneVerificationStatus.codeSent,
          verificationId: verificationId,
        );
      }
    } catch (e) {
      state = PhoneVerificationState(
        status: PhoneVerificationStatus.error,
        errorMessage: e.toString(),
      );
    }
  }

  /// Verify the OTP code.
  Future<void> verifyOtp(String code) async {
    final verificationId = state.verificationId;
    if (verificationId == null) {
      state = const PhoneVerificationState(
        status: PhoneVerificationStatus.error,
        errorMessage: 'No verification ID. Send OTP first.',
      );
      return;
    }

    state = state.copyWith(status: PhoneVerificationStatus.verifying);

    try {
      final authRepo = _ref.read(authRepositoryProvider);
      final user = await authRepo.verifyOtp(verificationId, code);
      state = PhoneVerificationState(
        status: PhoneVerificationStatus.verified,
        user: user,
      );
    } catch (e) {
      state = PhoneVerificationState(
        status: PhoneVerificationStatus.error,
        errorMessage: e.toString(),
      );
    }
  }

  /// Reset to idle state.
  void reset() {
    state = const PhoneVerificationState();
  }
}

/// Provider for the phone verification flow.
final phoneVerificationProvider =
    StateNotifierProvider<PhoneVerificationNotifier, PhoneVerificationState>(
  (ref) => PhoneVerificationNotifier(ref),
);

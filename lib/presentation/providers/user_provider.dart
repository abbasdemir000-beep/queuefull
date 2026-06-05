import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/models/app_user.dart';
import '../../domain/models/leaderboard_entry.dart';
import 'auth_provider.dart';
import 'cycle_provider.dart';
import 'repository_providers.dart';

/// Provider that derives the current [AppUser] from the auth state stream.
final currentUserProvider = Provider<AppUser?>((ref) {
  final authState = ref.watch(authStateProvider);
  return authState.valueOrNull;
});

/// Stream provider watching the leaderboard for the current cycle.
final leaderboardProvider = StreamProvider<List<LeaderboardEntry>>((ref) {
  final userRepo = ref.watch(userRepositoryProvider);
  final cycleState = ref.watch(cycleProvider);
  return userRepo.watchLeaderboard(cycleState.cycleStartTime);
});

/// Actions provider for user profile management.
class UserActionsNotifier extends StateNotifier<AsyncValue<void>> {
  UserActionsNotifier(this._ref) : super(const AsyncValue.data(null));

  final Ref _ref;

  /// Update the current user's profile.
  Future<void> updateProfile(AppUser updatedUser) async {
    state = const AsyncValue.loading();
    try {
      final userRepo = _ref.read(userRepositoryProvider);
      await userRepo.updateUser(updatedUser);
      state = const AsyncValue.data(null);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  /// Delete the current user's account.
  Future<void> deleteAccount() async {
    state = const AsyncValue.loading();
    try {
      final authRepo = _ref.read(authRepositoryProvider);
      await authRepo.deleteAccount();
      state = const AsyncValue.data(null);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }
}

/// Provider for user profile actions.
final userActionsProvider =
    StateNotifierProvider<UserActionsNotifier, AsyncValue<void>>(
  (ref) => UserActionsNotifier(ref),
);

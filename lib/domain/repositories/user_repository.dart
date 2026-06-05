import '../models/app_user.dart';
import '../models/leaderboard_entry.dart';

/// Abstract repository defining the user data contract.
/// Implementation will use Cloud Firestore.
abstract class UserRepository {
  /// Get a user by UID.
  Future<AppUser?> getUser(String uid);

  /// Create a new user document.
  Future<void> createUser(AppUser user);

  /// Update an existing user document.
  Future<void> updateUser(AppUser user);

  /// Watch leaderboard entries for the current cycle.
  Stream<List<LeaderboardEntry>> watchLeaderboard(DateTime cycleStart);

  /// Ban a user (admin action).
  Future<void> banUser(String uid);

  /// Unban a user (admin action).
  Future<void> unbanUser(String uid);

  /// Award points to a user.
  Future<void> awardPoints(String uid, int points);

  /// Watch all users as a real-time stream (admin use).
  Stream<List<AppUser>> watchAllUsers();

  /// Add points to a user (alias for awardPoints, identified by phone).
  Future<void> addPoints(String phoneOrUid, int points);
}

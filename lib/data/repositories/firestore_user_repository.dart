import 'package:cloud_firestore/cloud_firestore.dart';

import '../../core/constants/app_constants.dart';
import '../../domain/models/app_user.dart';
import '../../domain/models/leaderboard_entry.dart';
import '../../domain/repositories/user_repository.dart';

/// Firestore implementation of [UserRepository].
/// Uses the 'users' collection for all user data.
class FirestoreUserRepository implements UserRepository {
  FirestoreUserRepository({FirebaseFirestore? firestore})
      : _firestore = firestore ?? FirebaseFirestore.instance;

  final FirebaseFirestore _firestore;

  CollectionReference<Map<String, dynamic>> get _collection =>
      _firestore.collection(AppConstants.usersCollection);

  @override
  Future<AppUser?> getUser(String uid) async {
    final doc = await _collection.doc(uid).get();
    if (!doc.exists) return null;

    final data = doc.data()!;
    data['uid'] = doc.id;
    return AppUser.fromJson(data);
  }

  @override
  Future<void> createUser(AppUser user) async {
    await _collection.doc(user.uid).set(user.toJson());
  }

  @override
  Future<void> updateUser(AppUser user) async {
    await _collection.doc(user.uid).update(user.toJson());
  }

  @override
  Stream<List<LeaderboardEntry>> watchLeaderboard(DateTime cycleStart) {
    return _collection
        .where('banned', isEqualTo: false)
        .orderBy('points', descending: true)
        .limit(50)
        .snapshots()
        .map((snapshot) {
      final entries = <LeaderboardEntry>[];
      for (int i = 0; i < snapshot.docs.length; i++) {
        final data = snapshot.docs[i].data();
        entries.add(LeaderboardEntry(
          userPhone: data['phoneNumber'] as String? ?? '',
          userName: data['name'] as String? ?? '',
          points: data['points'] as int? ?? 0,
          rank: i + 1,
        ));
      }
      return entries;
    });
  }

  @override
  Future<void> banUser(String uid) async {
    await _collection.doc(uid).update({'banned': true});
  }

  @override
  Future<void> unbanUser(String uid) async {
    await _collection.doc(uid).update({'banned': false});
  }

  @override
  Future<void> awardPoints(String uid, int points) async {
    await _collection.doc(uid).update({
      'points': FieldValue.increment(points),
    });
  }
}

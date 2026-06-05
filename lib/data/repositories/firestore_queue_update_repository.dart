import 'package:cloud_firestore/cloud_firestore.dart';

import '../../core/constants/app_constants.dart';
import '../../domain/models/queue_update.dart';
import '../../domain/repositories/queue_update_repository.dart';

/// Firestore implementation of [QueueUpdateRepository].
/// Uses the 'queue_updates' collection for all queue update data.
class FirestoreQueueUpdateRepository implements QueueUpdateRepository {
  FirestoreQueueUpdateRepository({FirebaseFirestore? firestore})
      : _firestore = firestore ?? FirebaseFirestore.instance;

  final FirebaseFirestore _firestore;

  CollectionReference<Map<String, dynamic>> get _collection =>
      _firestore.collection(AppConstants.queueUpdatesCollection);

  @override
  Future<void> submitUpdate(QueueUpdate update) async {
    final json = update.toJson();
    json.remove('id'); // Let Firestore auto-generate the ID
    await _collection.add(json);
  }

  @override
  Stream<List<QueueUpdate>> watchUpdatesForStation(String stationId) {
    return _collection
        .where('stationId', isEqualTo: stationId)
        .orderBy('timestamp', descending: true)
        .snapshots()
        .map((snapshot) {
      return snapshot.docs.map((doc) {
        final data = doc.data();
        data['id'] = doc.id;
        return QueueUpdate.fromJson(data);
      }).toList();
    });
  }
}

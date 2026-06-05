import 'package:cloud_firestore/cloud_firestore.dart';

import '../../core/constants/app_constants.dart';
import '../../domain/models/station.dart';
import '../../domain/repositories/station_repository.dart';

/// Firestore implementation of [StationRepository].
/// Uses the 'stations' collection for all station data.
class FirestoreStationRepository implements StationRepository {
  FirestoreStationRepository({FirebaseFirestore? firestore})
      : _firestore = firestore ?? FirebaseFirestore.instance;

  final FirebaseFirestore _firestore;

  CollectionReference<Map<String, dynamic>> get _collection =>
      _firestore.collection(AppConstants.stationsCollection);

  @override
  Stream<List<Station>> watchStations() {
    return _collection.snapshots().map((snapshot) {
      return snapshot.docs.map((doc) {
        final data = doc.data();
        data['id'] = doc.id;
        return Station.fromJson(data);
      }).toList();
    });
  }

  @override
  Stream<List<Station>> watchStationsByCity(String cityId) {
    return _collection
        .where('cityId', isEqualTo: cityId)
        .snapshots()
        .map((snapshot) {
      return snapshot.docs.map((doc) {
        final data = doc.data();
        data['id'] = doc.id;
        return Station.fromJson(data);
      }).toList();
    });
  }

  @override
  Future<Station?> getStation(String id) async {
    final doc = await _collection.doc(id).get();
    if (!doc.exists) return null;

    final data = doc.data()!;
    data['id'] = doc.id;
    return Station.fromJson(data);
  }

  @override
  Future<void> suggestStation(Station station) async {
    final json = station.toJson();
    json['isApproved'] = false;
    json.remove('id'); // Let Firestore auto-generate the ID
    await _collection.add(json);
  }

  @override
  Future<void> updateStationStatus(
    String stationId,
    QueueStatus status,
    bool hasFuel,
  ) async {
    await _collection.doc(stationId).update({
      'queueStatus': status.toFirestore,
      'lastUpdated': DateTime.now().millisecondsSinceEpoch,
      'hasFuel': hasFuel,
      'confirmedCount': 0, // Reset confirmations on new status
    });
  }

  @override
  Future<void> confirmStatus(String stationId, String userPhone) async {
    await _collection.doc(stationId).update({
      'confirmedCount': FieldValue.increment(1),
    });
  }
}

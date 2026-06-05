import 'package:cloud_firestore/cloud_firestore.dart';

import '../../core/constants/app_constants.dart';
import '../../domain/models/city.dart';
import '../../domain/repositories/city_repository.dart';

/// Firestore implementation of [CityRepository].
/// Uses the 'cities' collection for all city data.
class FirestoreCityRepository implements CityRepository {
  FirestoreCityRepository({FirebaseFirestore? firestore})
      : _firestore = firestore ?? FirebaseFirestore.instance;

  final FirebaseFirestore _firestore;

  CollectionReference<Map<String, dynamic>> get _collection =>
      _firestore.collection(AppConstants.citiesCollection);

  @override
  Stream<List<City>> watchCities() {
    return _collection.snapshots().map((snapshot) {
      return snapshot.docs.map((doc) {
        final data = doc.data();
        data['id'] = doc.id;
        return City.fromJson(data);
      }).toList();
    });
  }

  @override
  Stream<List<City>> watchApprovedCities() {
    return _collection
        .where('isApproved', isEqualTo: true)
        .snapshots()
        .map((snapshot) {
      return snapshot.docs.map((doc) {
        final data = doc.data();
        data['id'] = doc.id;
        return City.fromJson(data);
      }).toList();
    });
  }

  @override
  Future<void> suggestCity(City city) async {
    final json = city.toJson();
    json['isApproved'] = false;
    json.remove('id'); // Let Firestore auto-generate the ID
    await _collection.add(json);
  }

  @override
  Future<void> approveCity(String cityId) async {
    await _collection.doc(cityId).update({'isApproved': true});
  }

  @override
  Future<void> deleteCity(String cityId) async {
    await _collection.doc(cityId).delete();
  }

  @override
  Future<void> addCity(City city) async {
    final json = city.toJson();
    json.remove('id'); // Let Firestore auto-generate the ID
    await _collection.add(json);
  }
}

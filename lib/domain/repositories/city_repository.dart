import '../models/city.dart';

/// Abstract repository defining the city data contract.
/// Implementation will use Cloud Firestore.
abstract class CityRepository {
  /// Watch all cities as a real-time stream.
  Stream<List<City>> watchCities();

  /// Watch only approved cities.
  Stream<List<City>> watchApprovedCities();

  /// Suggest a new city (pending admin approval).
  Future<void> suggestCity(City city);

  /// Approve a city (admin action).
  Future<void> approveCity(String cityId);

  /// Delete a city (admin action).
  Future<void> deleteCity(String cityId);
}

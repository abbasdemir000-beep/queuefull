import '../../core/constants/app_constants.dart';
import '../models/station.dart';

/// Abstract repository defining the station data contract.
/// Implementation will use Cloud Firestore.
abstract class StationRepository {
  /// Watch all stations as a real-time stream.
  Stream<List<Station>> watchStations();

  /// Watch stations filtered by city ID.
  Stream<List<Station>> watchStationsByCity(String cityId);

  /// Get a single station by ID.
  Future<Station?> getStation(String id);

  /// Suggest a new station (pending admin approval).
  Future<void> suggestStation(Station station);

  /// Update a station's queue status and fuel availability.
  Future<void> updateStationStatus(
    String stationId,
    QueueStatus status,
    bool hasFuel,
  );

  /// Confirm the current status of a station (user agreement).
  Future<void> confirmStatus(String stationId, String userPhone);

  /// Approve a pending station (admin action).
  Future<void> approveStation(String stationId);

  /// Delete a station (admin action).
  Future<void> deleteStation(String stationId);
}

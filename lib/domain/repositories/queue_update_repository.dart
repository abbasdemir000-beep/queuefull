import '../models/queue_update.dart';

/// Abstract repository defining the queue update data contract.
/// Implementation will use Cloud Firestore.
abstract class QueueUpdateRepository {
  /// Submit a new queue status update.
  Future<void> submitUpdate(QueueUpdate update);

  /// Watch updates for a specific station as a real-time stream.
  Stream<List<QueueUpdate>> watchUpdatesForStation(String stationId);
}

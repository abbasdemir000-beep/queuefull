import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/repositories/firebase_auth_repository.dart';
import '../../data/repositories/firebase_notification_repository.dart';
import '../../data/repositories/firestore_city_repository.dart';
import '../../data/repositories/firestore_queue_update_repository.dart';
import '../../data/repositories/firestore_station_repository.dart';
import '../../data/repositories/firestore_user_repository.dart';
import '../../domain/repositories/auth_repository.dart';
import '../../domain/repositories/city_repository.dart';
import '../../domain/repositories/notification_repository.dart';
import '../../domain/repositories/queue_update_repository.dart';
import '../../domain/repositories/station_repository.dart';
import '../../domain/repositories/user_repository.dart';

/// Central file providing all repository instances as Riverpod Providers.
/// These can be overridden in tests for mocking.

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return FirebaseAuthRepository();
});

final stationRepositoryProvider = Provider<StationRepository>((ref) {
  return FirestoreStationRepository();
});

final cityRepositoryProvider = Provider<CityRepository>((ref) {
  return FirestoreCityRepository();
});

final userRepositoryProvider = Provider<UserRepository>((ref) {
  return FirestoreUserRepository();
});

final queueUpdateRepositoryProvider = Provider<QueueUpdateRepository>((ref) {
  return FirestoreQueueUpdateRepository();
});

final notificationRepositoryProvider = Provider<NotificationRepository>((ref) {
  return FirebaseNotificationRepository();
});

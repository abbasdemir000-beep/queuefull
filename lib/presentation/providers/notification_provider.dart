import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/models/notification_item.dart';
import 'auth_provider.dart';
import 'repository_providers.dart';

/// Stream provider watching the current user's notifications.
final notificationsProvider = StreamProvider<List<NotificationItem>>((ref) {
  final authState = ref.watch(authStateProvider);
  final user = authState.valueOrNull;

  if (user == null) {
    return Stream.value([]);
  }

  final notificationRepo = ref.watch(notificationRepositoryProvider);
  return notificationRepo.watchNotifications(user.uid);
});

/// Provider derived from notificationsProvider counting unread notifications.
final unreadCountProvider = Provider<int>((ref) {
  final notificationsAsync = ref.watch(notificationsProvider);
  return notificationsAsync.when(
    data: (notifications) =>
        notifications.where((n) => !n.isRead).length,
    loading: () => 0,
    error: (_, __) => 0,
  );
});

/// Actions provider for notification operations.
class NotificationActionsNotifier extends StateNotifier<AsyncValue<void>> {
  NotificationActionsNotifier(this._ref)
      : super(const AsyncValue.data(null));

  final Ref _ref;

  /// Mark a notification as read.
  Future<void> markAsRead(String notificationId) async {
    try {
      final notificationRepo = _ref.read(notificationRepositoryProvider);
      await notificationRepo.markAsRead(notificationId);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  /// Request notification permission.
  Future<bool> requestPermission() async {
    final notificationRepo = _ref.read(notificationRepositoryProvider);
    return await notificationRepo.requestPermission();
  }

  /// Get the FCM token.
  Future<String?> getToken() async {
    final notificationRepo = _ref.read(notificationRepositoryProvider);
    return await notificationRepo.getToken();
  }
}

/// Provider for notification actions.
final notificationActionsProvider =
    StateNotifierProvider<NotificationActionsNotifier, AsyncValue<void>>(
  (ref) => NotificationActionsNotifier(ref),
);

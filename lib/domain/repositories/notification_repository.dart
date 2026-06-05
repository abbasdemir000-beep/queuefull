import '../models/notification_item.dart';

/// Abstract repository defining the notification data contract.
/// Implementation will use Cloud Firestore and Firebase Messaging.
abstract class NotificationRepository {
  /// Watch notifications for a user as a real-time stream.
  Stream<List<NotificationItem>> watchNotifications(String userId);

  /// Mark a notification as read.
  Future<void> markAsRead(String notificationId);

  /// Request notification permission from the user.
  Future<bool> requestPermission();

  /// Get the FCM token for push notifications.
  Future<String?> getToken();
}

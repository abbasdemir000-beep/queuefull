import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../domain/models/notification_item.dart';
import '../../providers/notification_provider.dart';

/// Notification screen showing all user notifications.
class NotificationScreen extends ConsumerWidget {
  const NotificationScreen({super.key});

  IconData _iconForType(String type) {
    switch (type) {
      case 'points':
        return Icons.star;
      case 'status':
        return Icons.local_gas_station;
      case 'admin':
        return Icons.admin_panel_settings;
      case 'cycle':
        return Icons.timer;
      default:
        return Icons.notifications;
    }
  }

  String _timeAgo(DateTime dateTime) {
    final diff = DateTime.now().difference(dateTime);
    if (diff.inMinutes < 1) return '\u0627\u0644\u0622\u0646';
    if (diff.inMinutes < 60) {
      return '\u0645\u0646\u0630 ${diff.inMinutes} \u062F\u0642\u064A\u0642\u0629';
    }
    if (diff.inHours < 24) {
      return '\u0645\u0646\u0630 ${diff.inHours} \u0633\u0627\u0639\u0629';
    }
    return '\u0645\u0646\u0630 ${diff.inDays} \u064A\u0648\u0645';
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notificationsAsync = ref.watch(notificationsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('\u0627\u0644\u0625\u0634\u0639\u0627\u0631\u0627\u062A'),
      ),
      body: notificationsAsync.when(
        data: (notifications) {
          if (notifications.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.notifications_off, size: 64, color: Colors.grey[400]),
                  const SizedBox(height: 16),
                  Text(
                    '\u0644\u0627 \u062A\u0648\u062C\u062F \u0625\u0634\u0639\u0627\u0631\u0627\u062A',
                    style: TextStyle(
                      fontSize: 18,
                      color: Colors.grey[600],
                    ),
                  ),
                ],
              ),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.symmetric(vertical: 8),
            itemCount: notifications.length,
            itemBuilder: (context, index) {
              final notification = notifications[index];
              return _NotificationTile(
                notification: notification,
                icon: _iconForType(notification.type),
                timeAgo: _timeAgo(notification.createdAt),
                onTap: () {
                  if (!notification.isRead) {
                    ref
                        .read(notificationActionsProvider.notifier)
                        .markAsRead(notification.id);
                  }
                },
              );
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('\u062E\u0637\u0623: $e')),
      ),
    );
  }
}

class _NotificationTile extends StatelessWidget {
  const _NotificationTile({
    required this.notification,
    required this.icon,
    required this.timeAgo,
    required this.onTap,
  });

  final NotificationItem notification;
  final IconData icon;
  final String timeAgo;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Container(
      color: notification.isRead ? null : Colors.teal.withOpacity(0.05),
      child: ListTile(
        onTap: onTap,
        leading: CircleAvatar(
          backgroundColor:
              notification.isRead ? Colors.grey[200] : Colors.teal[50],
          child: Icon(
            icon,
            color: notification.isRead ? Colors.grey : Colors.teal,
            size: 20,
          ),
        ),
        title: Text(
          notification.title,
          style: TextStyle(
            fontWeight: notification.isRead ? FontWeight.normal : FontWeight.bold,
          ),
        ),
        subtitle: Text(
          notification.body,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
        ),
        trailing: Text(
          timeAgo,
          style: TextStyle(fontSize: 11, color: Colors.grey[500]),
        ),
      ),
    );
  }
}

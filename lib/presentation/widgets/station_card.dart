import 'package:flutter/material.dart';

import '../../core/constants/app_constants.dart';
import '../../domain/models/station.dart';
import 'queue_status_chip.dart';

/// Reusable card widget for displaying station info in lists.
class StationCard extends StatelessWidget {
  const StationCard({
    super.key,
    required this.station,
    this.onTap,
  });

  final Station station;
  final VoidCallback? onTap;

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
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      child: ListTile(
        onTap: onTap,
        leading: Icon(
          station.type == StationType.government
              ? Icons.account_balance
              : Icons.local_gas_station,
          color: station.type == StationType.government
              ? Colors.blue
              : Colors.orange,
          size: 32,
        ),
        title: Text(
          station.name,
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 4),
            Row(
              children: [
                QueueStatusChip(status: station.queueStatus),
                const SizedBox(width: 8),
                Icon(
                  station.hasFuel ? Icons.local_gas_station : Icons.block,
                  size: 16,
                  color: station.hasFuel ? Colors.green : Colors.red,
                ),
                const SizedBox(width: 4),
                Text(
                  station.hasFuel
                      ? '\u0645\u062A\u0648\u0641\u0631'
                      : '\u063A\u064A\u0631 \u0645\u062A\u0648\u0641\u0631',
                  style: TextStyle(
                    fontSize: 12,
                    color: station.hasFuel ? Colors.green : Colors.red,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Row(
              children: [
                Text(
                  _timeAgo(station.lastUpdated),
                  style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                ),
                const SizedBox(width: 12),
                Icon(Icons.people, size: 14, color: Colors.grey[600]),
                const SizedBox(width: 2),
                Text(
                  '${station.confirmedCount}',
                  style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                ),
              ],
            ),
          ],
        ),
        isThreeLine: true,
        trailing: const Icon(Icons.chevron_right),
      ),
    );
  }
}

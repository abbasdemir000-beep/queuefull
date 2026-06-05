import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/models/station.dart';
import '../../providers/location_provider.dart';
import '../../providers/station_provider.dart';
import '../../providers/user_provider.dart';
import '../../widgets/gps_indicator.dart';
import '../../widgets/queue_status_chip.dart';

/// Station detail screen showing full station information.
class StationDetailScreen extends ConsumerWidget {
  const StationDetailScreen({super.key, required this.stationId});

  final String stationId;

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
    final stationAsync = ref.watch(stationProvider(stationId));

    return Scaffold(
      appBar: AppBar(
        title: const Text('\u062A\u0641\u0627\u0635\u064A\u0644 \u0627\u0644\u0645\u062D\u0637\u0629'),
      ),
      body: stationAsync.when(
        data: (station) {
          if (station == null) {
            return const Center(
              child: Text('\u0627\u0644\u0645\u062D\u0637\u0629 \u063A\u064A\u0631 \u0645\u0648\u062C\u0648\u062F\u0629'),
            );
          }
          return _StationDetailBody(station: station);
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('\u062E\u0637\u0623: $e')),
      ),
    );
  }
}

class _StationDetailBody extends ConsumerWidget {
  const _StationDetailBody({required this.station});

  final Station station;

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
    final isWithinRange = ref.watch(
      proximityProvider((lat: station.latitude, lng: station.longitude)),
    );
    final currentUser = ref.watch(currentUserProvider);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Station name and type
          Row(
            children: [
              Expanded(
                child: Text(
                  station.name,
                  style: const TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              Chip(
                label: Text(
                  station.type.displayName,
                  style: const TextStyle(fontSize: 12),
                ),
                backgroundColor: station.type == StationType.government
                    ? Colors.blue[50]
                    : Colors.orange[50],
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Mini map
          ClipRRect(
            borderRadius: BorderRadius.circular(12),
            child: SizedBox(
              height: 180,
              child: GoogleMap(
                initialCameraPosition: CameraPosition(
                  target: LatLng(station.latitude, station.longitude),
                  zoom: 15,
                ),
                markers: {
                  Marker(
                    markerId: MarkerId(station.id),
                    position: LatLng(station.latitude, station.longitude),
                  ),
                },
                zoomControlsEnabled: false,
                scrollGesturesEnabled: false,
                rotateGesturesEnabled: false,
                tiltGesturesEnabled: false,
                liteModeEnabled: true,
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Queue status
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  const Text(
                    '\u062D\u0627\u0644\u0629 \u0627\u0644\u0637\u0627\u0628\u0648\u0631:',
                    style: TextStyle(fontSize: 16),
                  ),
                  const SizedBox(width: 12),
                  QueueStatusChip(status: station.queueStatus, large: true),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // Fuel availability
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  Icon(
                    station.hasFuel ? Icons.check_circle : Icons.cancel,
                    color: station.hasFuel ? Colors.green : Colors.red,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    station.hasFuel
                        ? '\u0627\u0644\u0648\u0642\u0648\u062F \u0645\u062A\u0648\u0641\u0631'
                        : '\u0627\u0644\u0648\u0642\u0648\u062F \u063A\u064A\u0631 \u0645\u062A\u0648\u0641\u0631',
                    style: const TextStyle(fontSize: 16),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // Fuel types
          if (station.fuelTypes.isNotEmpty) ...[
            Card(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '\u0623\u0646\u0648\u0627\u0639 \u0627\u0644\u0648\u0642\u0648\u062F:',
                      style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 8,
                      children: station.fuelTypes
                          .map((ft) => Chip(label: Text(ft.displayName)))
                          .toList(),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),
          ],

          // Last updated and confirmed count
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                children: [
                  Row(
                    children: [
                      const Icon(Icons.access_time, size: 18, color: Colors.grey),
                      const SizedBox(width: 8),
                      Text(
                        '\u0622\u062E\u0631 \u062A\u062D\u062F\u064A\u062B: ${_timeAgo(station.lastUpdated)}',
                        style: const TextStyle(fontSize: 14),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.people, size: 18, color: Colors.grey),
                      const SizedBox(width: 8),
                      Text(
                        '${station.confirmedCount} \u0645\u0631\u0627\u0633\u0644 \u0623\u0643\u062F\u0648\u0627',
                        style: const TextStyle(fontSize: 14),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // GPS indicator
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: GpsIndicator(
                stationLat: station.latitude,
                stationLng: station.longitude,
              ),
            ),
          ),
          const SizedBox(height: 24),

          // Action buttons
          Row(
            children: [
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: isWithinRange
                      ? () => context.push('/station/${station.id}/report')
                      : null,
                  icon: const Icon(Icons.edit_note),
                  label: const Text('\u0625\u0628\u0644\u0627\u063A \u0627\u0644\u062D\u0627\u0644\u0629'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: isWithinRange
                      ? () async {
                          if (currentUser == null) return;
                          await ref
                              .read(stationActionsProvider.notifier)
                              .confirmStatus(
                                stationId: station.id,
                                userPhone: currentUser.phoneNumber,
                              );
                          if (context.mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text(
                                  '\u062A\u0645 \u0627\u0644\u062A\u0623\u0643\u064A\u062F! +5 \u0646\u0642\u0627\u0637',
                                ),
                                backgroundColor: Colors.green,
                              ),
                            );
                          }
                        }
                      : null,
                  icon: const Icon(Icons.check_circle),
                  label: const Text('\u062A\u0623\u0643\u064A\u062F \u0627\u0644\u062D\u0627\u0644\u0629'),
                ),
              ),
            ],
          ),
          if (!isWithinRange) ...[
            const SizedBox(height: 8),
            const Text(
              '\u064A\u062C\u0628 \u0623\u0646 \u062A\u0643\u0648\u0646 \u0636\u0645\u0646 200 \u0645\u062A\u0631 \u0645\u0646 \u0627\u0644\u0645\u062D\u0637\u0629',
              style: TextStyle(color: Colors.orange, fontSize: 12),
              textAlign: TextAlign.center,
            ),
          ],
        ],
      ),
    );
  }
}

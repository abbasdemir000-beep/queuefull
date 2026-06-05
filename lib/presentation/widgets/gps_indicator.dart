import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/use_cases/geo_proximity_policy.dart';
import '../providers/location_provider.dart';

/// Widget that shows GPS proximity status relative to a station.
class GpsIndicator extends ConsumerWidget {
  const GpsIndicator({
    super.key,
    required this.stationLat,
    required this.stationLng,
  });

  final double stationLat;
  final double stationLng;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final locationAsync = ref.watch(currentLocationProvider);

    return locationAsync.when(
      data: (position) {
        final distance = GeoProximityPolicy.distanceMeters(
          position.latitude,
          position.longitude,
          stationLat,
          stationLng,
        );
        final isWithin = distance <= GeoProximityPolicy.defaultRadiusMeters;
        final distanceText = distance < 1000
            ? '${distance.toInt()}m'
            : '${(distance / 1000).toStringAsFixed(1)}km';

        return Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              isWithin ? Icons.check_circle : Icons.cancel,
              color: isWithin ? Colors.green : Colors.red,
              size: 20,
            ),
            const SizedBox(width: 6),
            Text(
              isWithin
                  ? '\u0636\u0645\u0646 \u0627\u0644\u0646\u0637\u0627\u0642 ($distanceText)'
                  : '\u0628\u0639\u064A\u062F \u062C\u062F\u0627 ($distanceText)',
              style: TextStyle(
                color: isWithin ? Colors.green : Colors.red,
                fontSize: 13,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        );
      },
      loading: () => const Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(
            width: 16,
            height: 16,
            child: CircularProgressIndicator(strokeWidth: 2),
          ),
          SizedBox(width: 6),
          Text(
            '\u062C\u0627\u0631\u064A \u062A\u062D\u062F\u064A\u062F \u0627\u0644\u0645\u0648\u0642\u0639...',
            style: TextStyle(fontSize: 13, color: Colors.grey),
          ),
        ],
      ),
      error: (e, _) => Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.location_off, color: Colors.orange, size: 20),
          const SizedBox(width: 6),
          Text(
            '\u062A\u0639\u0630\u0631 \u062A\u062D\u062F\u064A\u062F \u0627\u0644\u0645\u0648\u0642\u0639',
            style: TextStyle(fontSize: 13, color: Colors.orange[700]),
          ),
        ],
      ),
    );
  }
}

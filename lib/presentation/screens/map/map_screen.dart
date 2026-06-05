import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/models/station.dart';
import '../../providers/city_provider.dart';
import '../../providers/location_provider.dart';
import '../../providers/station_provider.dart';
import '../../widgets/queue_status_chip.dart';

/// Map screen showing stations as colored markers.
class MapScreen extends ConsumerStatefulWidget {
  const MapScreen({super.key});

  @override
  ConsumerState<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends ConsumerState<MapScreen> {
  GoogleMapController? _mapController;

  // City coordinates
  static const Map<String, LatLng> _cityCoordinates = {
    'Kirkuk': LatLng(35.4681, 44.3922),
    'Erbil': LatLng(36.1901, 44.0091),
    'Sulaymaniyah': LatLng(35.5570, 45.4353),
  };

  // Default center (Kirkuk)
  static const LatLng _defaultCenter = LatLng(35.4681, 44.3922);

  Set<Marker> _buildMarkers(List<Station> stations) {
    return stations.map((station) {
      return Marker(
        markerId: MarkerId(station.id),
        position: LatLng(station.latitude, station.longitude),
        icon: BitmapDescriptor.defaultMarkerWithHue(_hueForStatus(station.queueStatus)),
        onTap: () => _showStationBottomSheet(station),
      );
    }).toSet();
  }

  double _hueForStatus(QueueStatus status) {
    switch (status) {
      case QueueStatus.empty:
        return BitmapDescriptor.hueGreen;
      case QueueStatus.short_:
        return BitmapDescriptor.hueYellow;
      case QueueStatus.moderate:
        return BitmapDescriptor.hueOrange;
      case QueueStatus.long_:
        return BitmapDescriptor.hueRed;
      case QueueStatus.closed:
        return BitmapDescriptor.hueViolet;
    }
  }

  void _showStationBottomSheet(Station station) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (ctx) {
        return Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                station.name,
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  QueueStatusChip(status: station.queueStatus),
                  const Spacer(),
                  Text(
                    _timeAgo(station.lastUpdated),
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.grey[600],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () {
                    Navigator.pop(ctx);
                    context.push('/station/${station.id}');
                  },
                  child: const Text('\u0639\u0631\u0636 \u0627\u0644\u062A\u0641\u0627\u0635\u064A\u0644'),
                ),
              ),
            ],
          ),
        );
      },
    );
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

  void _goToUserLocation() async {
    final location = ref.read(currentLocationProvider);
    location.when(
      data: (pos) {
        _mapController?.animateCamera(
          CameraUpdate.newLatLng(LatLng(pos.latitude, pos.longitude)),
        );
      },
      loading: () {},
      error: (_, __) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('\u062A\u0639\u0630\u0631 \u062A\u062D\u062F\u064A\u062F \u0627\u0644\u0645\u0648\u0642\u0639'),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final selectedCity = ref.watch(selectedCityProvider);
    final stationsAsync = selectedCity != null
        ? ref.watch(stationsByCityProvider(selectedCity))
        : ref.watch(stationsProvider);

    // Determine map center based on selected city
    LatLng center = _defaultCenter;
    if (selectedCity != null) {
      // Try to match city name to coordinates
      for (final entry in _cityCoordinates.entries) {
        if (selectedCity.toLowerCase().contains(entry.key.toLowerCase())) {
          center = entry.value;
          break;
        }
      }
    }

    return Scaffold(
      body: stationsAsync.when(
        data: (stations) {
          final markers = _buildMarkers(stations);
          return GoogleMap(
            initialCameraPosition: CameraPosition(
              target: center,
              zoom: 13,
            ),
            markers: markers,
            myLocationEnabled: true,
            myLocationButtonEnabled: false,
            onMapCreated: (controller) {
              _mapController = controller;
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(
          child: Text('\u062E\u0637\u0623: $e'),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        mini: true,
        onPressed: _goToUserLocation,
        child: const Icon(Icons.my_location),
      ),
    );
  }
}

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:geolocator/geolocator.dart';

import '../../domain/use_cases/geo_proximity_policy.dart';

/// Future provider that checks and requests location permission.
final locationPermissionProvider = FutureProvider<LocationPermission>((ref) async {
  LocationPermission permission = await Geolocator.checkPermission();
  if (permission == LocationPermission.denied) {
    permission = await Geolocator.requestPermission();
  }
  return permission;
});

/// Future provider that gets the current GPS position.
/// Requires location permission to be granted first.
final currentLocationProvider = FutureProvider<Position>((ref) async {
  // Ensure permission is requested
  final permission = await ref.watch(locationPermissionProvider.future);

  if (permission == LocationPermission.denied ||
      permission == LocationPermission.deniedForever) {
    throw Exception('Location permission denied');
  }

  return await Geolocator.getCurrentPosition(
    desiredAccuracy: LocationAccuracy.high,
  );
});

/// Provider family that checks if the user is within 200m of a station.
/// Parameters are the station's latitude and longitude.
final proximityProvider =
    Provider.family<bool, ({double lat, double lng})>((ref, stationCoords) {
  final locationAsync = ref.watch(currentLocationProvider);

  return locationAsync.when(
    data: (position) => GeoProximityPolicy.isWithinRadius(
      position.latitude,
      position.longitude,
      stationCoords.lat,
      stationCoords.lng,
    ),
    loading: () => false,
    error: (_, __) => false,
  );
});

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/models/city.dart';
import 'repository_providers.dart';

/// Stream provider watching approved cities only.
final citiesProvider = StreamProvider<List<City>>((ref) {
  final cityRepo = ref.watch(cityRepositoryProvider);
  return cityRepo.watchApprovedCities();
});

/// Stream provider watching all cities (including unapproved) for admin use.
final allCitiesProvider = StreamProvider<List<City>>((ref) {
  final cityRepo = ref.watch(cityRepositoryProvider);
  return cityRepo.watchCities();
});

/// State provider for the currently selected city filter.
final selectedCityProvider = StateProvider<String?>((ref) => null);

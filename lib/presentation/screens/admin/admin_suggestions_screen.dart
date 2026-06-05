import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/models/city.dart';
import '../../../domain/models/station.dart';
import '../../providers/city_provider.dart';
import '../../providers/repository_providers.dart';
import '../../providers/station_provider.dart';

/// Admin screen for managing pending suggestions.
class AdminSuggestionsScreen extends ConsumerWidget {
  const AdminSuggestionsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stationsAsync = ref.watch(stationsProvider);
    final citiesAsync = ref.watch(allCitiesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('\u0627\u0644\u0627\u0642\u062A\u0631\u0627\u062D\u0627\u062A \u0627\u0644\u0645\u0639\u0644\u0642\u0629'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Station suggestions
            const Text(
              '\u0627\u0642\u062A\u0631\u0627\u062D\u0627\u062A \u0627\u0644\u0645\u062D\u0637\u0627\u062A',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            stationsAsync.when(
              data: (stations) {
                final pending =
                    stations.where((s) => !s.isApproved).toList();
                if (pending.isEmpty) {
                  return const Padding(
                    padding: EdgeInsets.all(16),
                    child: Text(
                      '\u0644\u0627 \u062A\u0648\u062C\u062F \u0627\u0642\u062A\u0631\u0627\u062D\u0627\u062A \u0645\u062D\u0637\u0627\u062A',
                      style: TextStyle(color: Colors.grey),
                    ),
                  );
                }
                return Column(
                  children: pending
                      .map((s) => _StationSuggestionCard(station: s, ref: ref))
                      .toList(),
                );
              },
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Text('\u062E\u0637\u0623: $e'),
            ),

            const SizedBox(height: 24),
            const Divider(),
            const SizedBox(height: 16),

            // City suggestions
            const Text(
              '\u0627\u0642\u062A\u0631\u0627\u062D\u0627\u062A \u0627\u0644\u0645\u062F\u0646',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            citiesAsync.when(
              data: (cities) {
                final pending =
                    cities.where((c) => !c.isApproved).toList();
                if (pending.isEmpty) {
                  return const Padding(
                    padding: EdgeInsets.all(16),
                    child: Text(
                      '\u0644\u0627 \u062A\u0648\u062C\u062F \u0627\u0642\u062A\u0631\u0627\u062D\u0627\u062A \u0645\u062F\u0646',
                      style: TextStyle(color: Colors.grey),
                    ),
                  );
                }
                return Column(
                  children: pending
                      .map((c) => _CitySuggestionCard(city: c, ref: ref))
                      .toList(),
                );
              },
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Text('\u062E\u0637\u0623: $e'),
            ),
          ],
        ),
      ),
    );
  }
}

class _StationSuggestionCard extends StatelessWidget {
  const _StationSuggestionCard({required this.station, required this.ref});

  final Station station;
  final WidgetRef ref;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              station.name,
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 4),
            Text(
              '${station.cityName} - ${station.type.displayName}',
              style: TextStyle(color: Colors.grey[600], fontSize: 13),
            ),
            if (station.suggestedBy != null) ...[
              const SizedBox(height: 4),
              Text(
                '\u0627\u0642\u062A\u0631\u062D\u0647\u0627: ${station.suggestedBy}',
                style: TextStyle(color: Colors.grey[600], fontSize: 12),
              ),
            ],
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton.icon(
                  onPressed: () async {
                    final stationRepo = ref.read(stationRepositoryProvider);
                    await stationRepo.deleteStation(station.id);
                  },
                  icon: const Icon(Icons.close, color: Colors.red, size: 18),
                  label: const Text(
                    '\u0631\u0641\u0636',
                    style: TextStyle(color: Colors.red),
                  ),
                ),
                const SizedBox(width: 8),
                ElevatedButton.icon(
                  onPressed: () async {
                    final stationRepo = ref.read(stationRepositoryProvider);
                    await stationRepo.approveStation(station.id);
                    // Award points
                    if (station.suggestedBy != null) {
                      final userRepo = ref.read(userRepositoryProvider);
                      await userRepo.addPoints(
                        station.suggestedBy!,
                        AppConstants.stationApprovalPoints,
                      );
                    }
                  },
                  icon: const Icon(Icons.check, size: 18),
                  label: const Text('\u0642\u0628\u0648\u0644'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green,
                    foregroundColor: Colors.white,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _CitySuggestionCard extends StatelessWidget {
  const _CitySuggestionCard({required this.city, required this.ref});

  final City city;
  final WidgetRef ref;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    city.nameAr,
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                  Text(
                    city.nameEn,
                    style: TextStyle(color: Colors.grey[600], fontSize: 13),
                  ),
                ],
              ),
            ),
            TextButton.icon(
              onPressed: () async {
                final cityRepo = ref.read(cityRepositoryProvider);
                await cityRepo.deleteCity(city.id);
              },
              icon: const Icon(Icons.close, color: Colors.red, size: 18),
              label: const Text(
                '\u0631\u0641\u0636',
                style: TextStyle(color: Colors.red),
              ),
            ),
            const SizedBox(width: 4),
            ElevatedButton.icon(
              onPressed: () async {
                final cityRepo = ref.read(cityRepositoryProvider);
                await cityRepo.approveCity(city.id);
              },
              icon: const Icon(Icons.check, size: 18),
              label: const Text('\u0642\u0628\u0648\u0644'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.green,
                foregroundColor: Colors.white,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

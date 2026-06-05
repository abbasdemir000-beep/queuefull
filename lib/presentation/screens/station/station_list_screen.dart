import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/city_provider.dart';
import '../../providers/station_provider.dart';
import '../../widgets/station_card.dart';

/// Station list screen showing all stations for the selected city.
class StationListScreen extends ConsumerWidget {
  const StationListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedCity = ref.watch(selectedCityProvider);
    final stationsAsync = selectedCity != null
        ? ref.watch(stationsByCityProvider(selectedCity))
        : ref.watch(stationsProvider);

    return stationsAsync.when(
      data: (stations) {
        if (stations.isEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.local_gas_station, size: 64, color: Colors.grey[400]),
                const SizedBox(height: 16),
                Text(
                  '\u0644\u0627 \u062A\u0648\u062C\u062F \u0645\u062D\u0637\u0627\u062A',
                  style: TextStyle(
                    fontSize: 18,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          );
        }

        return RefreshIndicator(
          onRefresh: () async {
            // Invalidate provider to trigger re-fetch
            if (selectedCity != null) {
              ref.invalidate(stationsByCityProvider(selectedCity));
            } else {
              ref.invalidate(stationsProvider);
            }
          },
          child: ListView.builder(
            padding: const EdgeInsets.symmetric(vertical: 8),
            itemCount: stations.length,
            itemBuilder: (context, index) {
              final station = stations[index];
              return StationCard(
                station: station,
                onTap: () => context.push('/station/${station.id}'),
              );
            },
          ),
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 48, color: Colors.red),
            const SizedBox(height: 16),
            Text('\u062E\u0637\u0623: $e'),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                if (selectedCity != null) {
                  ref.invalidate(stationsByCityProvider(selectedCity));
                } else {
                  ref.invalidate(stationsProvider);
                }
              },
              child: const Text('\u0625\u0639\u0627\u062F\u0629 \u0627\u0644\u0645\u062D\u0627\u0648\u0644\u0629'),
            ),
          ],
        ),
      ),
    );
  }
}

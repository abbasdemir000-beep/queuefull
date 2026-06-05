import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/models/station.dart';
import '../../providers/repository_providers.dart';
import '../../providers/station_provider.dart';

/// Admin screen for managing stations.
class AdminStationsScreen extends ConsumerWidget {
  const AdminStationsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stationsAsync = ref.watch(stationsProvider);

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('\u0625\u062F\u0627\u0631\u0629 \u0627\u0644\u0645\u062D\u0637\u0627\u062A'),
          bottom: const TabBar(
            tabs: [
              Tab(text: '\u0627\u0644\u0645\u0639\u062A\u0645\u062F\u0629'),
              Tab(text: '\u0627\u0644\u0645\u0639\u0644\u0642\u0629'),
            ],
          ),
        ),
        body: stationsAsync.when(
          data: (stations) {
            final approved =
                stations.where((s) => s.isApproved).toList();
            final pending =
                stations.where((s) => !s.isApproved).toList();

            return TabBarView(
              children: [
                _ApprovedStationsTab(stations: approved, ref: ref),
                _PendingStationsTab(stations: pending, ref: ref),
              ],
            );
          },
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => Center(child: Text('\u062E\u0637\u0623: $e')),
        ),
      ),
    );
  }
}

class _ApprovedStationsTab extends StatelessWidget {
  const _ApprovedStationsTab({required this.stations, required this.ref});

  final List<Station> stations;
  final WidgetRef ref;

  @override
  Widget build(BuildContext context) {
    if (stations.isEmpty) {
      return const Center(
        child: Text('\u0644\u0627 \u062A\u0648\u062C\u062F \u0645\u062D\u0637\u0627\u062A \u0645\u0639\u062A\u0645\u062F\u0629'),
      );
    }

    return ListView.builder(
      itemCount: stations.length,
      itemBuilder: (context, index) {
        final station = stations[index];
        return ListTile(
          title: Text(station.name),
          subtitle: Text('${station.cityName} - ${station.type.displayName}'),
          trailing: IconButton(
            icon: const Icon(Icons.delete, color: Colors.red),
            onPressed: () => _confirmDelete(context, station),
          ),
        );
      },
    );
  }

  void _confirmDelete(BuildContext context, Station station) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('\u062D\u0630\u0641 \u0627\u0644\u0645\u062D\u0637\u0629'),
        content: Text('\u0647\u0644 \u0623\u0646\u062A \u0645\u062A\u0623\u0643\u062F \u0645\u0646 \u062D\u0630\u0641 "${station.name}"\u061F'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('\u0625\u0644\u063A\u0627\u0621'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(ctx);
              final stationRepo = ref.read(stationRepositoryProvider);
              await stationRepo.deleteStation(station.id);
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('\u062D\u0630\u0641'),
          ),
        ],
      ),
    );
  }
}

class _PendingStationsTab extends StatelessWidget {
  const _PendingStationsTab({required this.stations, required this.ref});

  final List<Station> stations;
  final WidgetRef ref;

  @override
  Widget build(BuildContext context) {
    if (stations.isEmpty) {
      return const Center(
        child: Text('\u0644\u0627 \u062A\u0648\u062C\u062F \u0645\u062D\u0637\u0627\u062A \u0645\u0639\u0644\u0642\u0629'),
      );
    }

    return ListView.builder(
      itemCount: stations.length,
      itemBuilder: (context, index) {
        final station = stations[index];
        return Card(
          margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          child: ListTile(
            title: Text(station.name),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('${station.cityName} - ${station.type.displayName}'),
                if (station.suggestedBy != null)
                  Text(
                    '\u0627\u0642\u062A\u0631\u062D\u0647\u0627: ${station.suggestedBy}',
                    style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                  ),
              ],
            ),
            isThreeLine: station.suggestedBy != null,
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: const Icon(Icons.check_circle, color: Colors.green),
                  onPressed: () async {
                    final stationRepo = ref.read(stationRepositoryProvider);
                    await stationRepo.approveStation(station.id);
                    // Award points to suggester
                    if (station.suggestedBy != null) {
                      final userRepo = ref.read(userRepositoryProvider);
                      await userRepo.addPoints(
                        station.suggestedBy!,
                        AppConstants.stationApprovalPoints,
                      );
                    }
                  },
                ),
                IconButton(
                  icon: const Icon(Icons.cancel, color: Colors.red),
                  onPressed: () async {
                    final stationRepo = ref.read(stationRepositoryProvider);
                    await stationRepo.deleteStation(station.id);
                  },
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

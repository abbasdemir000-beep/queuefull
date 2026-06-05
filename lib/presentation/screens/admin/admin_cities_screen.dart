import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../domain/models/city.dart';
import '../../providers/city_provider.dart';
import '../../providers/repository_providers.dart';

/// Admin screen for managing cities.
class AdminCitiesScreen extends ConsumerWidget {
  const AdminCitiesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final citiesAsync = ref.watch(allCitiesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('\u0625\u062F\u0627\u0631\u0629 \u0627\u0644\u0645\u062F\u0646'),
      ),
      body: citiesAsync.when(
        data: (cities) {
          if (cities.isEmpty) {
            return const Center(
              child: Text('\u0644\u0627 \u062A\u0648\u062C\u062F \u0645\u062F\u0646'),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.symmetric(vertical: 8),
            itemCount: cities.length,
            itemBuilder: (context, index) {
              final city = cities[index];
              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                child: ListTile(
                  title: Text(city.nameAr),
                  subtitle: Text(city.nameEn),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      if (!city.isApproved) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.orange[50],
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text(
                            '\u0645\u0639\u0644\u0642\u0629',
                            style: TextStyle(
                              color: Colors.orange,
                              fontSize: 12,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        IconButton(
                          icon: const Icon(
                            Icons.check_circle,
                            color: Colors.green,
                          ),
                          onPressed: () async {
                            final cityRepo = ref.read(cityRepositoryProvider);
                            await cityRepo.approveCity(city.id);
                          },
                        ),
                      ],
                      IconButton(
                        icon: const Icon(Icons.delete, color: Colors.red),
                        onPressed: () =>
                            _confirmDelete(context, ref, city),
                      ),
                    ],
                  ),
                ),
              );
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('\u062E\u0637\u0623: $e')),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddCityDialog(context, ref),
        child: const Icon(Icons.add),
      ),
    );
  }

  void _showAddCityDialog(BuildContext context, WidgetRef ref) {
    final nameArController = TextEditingController();
    final nameEnController = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('\u0625\u0636\u0627\u0641\u0629 \u0645\u062F\u064A\u0646\u0629'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameArController,
              decoration: const InputDecoration(
                labelText: '\u0627\u0644\u0627\u0633\u0645 \u0628\u0627\u0644\u0639\u0631\u0628\u064A\u0629',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: nameEnController,
              decoration: const InputDecoration(
                labelText: '\u0627\u0644\u0627\u0633\u0645 \u0628\u0627\u0644\u0625\u0646\u062C\u0644\u064A\u0632\u064A\u0629',
                border: OutlineInputBorder(),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('\u0625\u0644\u063A\u0627\u0621'),
          ),
          ElevatedButton(
            onPressed: () async {
              final nameAr = nameArController.text.trim();
              final nameEn = nameEnController.text.trim();
              if (nameAr.isEmpty || nameEn.isEmpty) return;

              Navigator.pop(ctx);
              final cityRepo = ref.read(cityRepositoryProvider);
              await cityRepo.addCity(City(
                id: '',
                nameAr: nameAr,
                nameEn: nameEn,
                isApproved: true,
              ));
            },
            child: const Text('\u0625\u0636\u0627\u0641\u0629'),
          ),
        ],
      ),
    );
  }

  void _confirmDelete(BuildContext context, WidgetRef ref, City city) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('\u062D\u0630\u0641 \u0627\u0644\u0645\u062F\u064A\u0646\u0629'),
        content: Text('\u0647\u0644 \u0623\u0646\u062A \u0645\u062A\u0623\u0643\u062F \u0645\u0646 \u062D\u0630\u0641 "${city.nameAr}"\u061F'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('\u0625\u0644\u063A\u0627\u0621'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(ctx);
              final cityRepo = ref.read(cityRepositoryProvider);
              await cityRepo.deleteCity(city.id);
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('\u062D\u0630\u0641'),
          ),
        ],
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/models/city.dart';
import '../providers/city_provider.dart';

/// Dropdown widget for selecting the active city.
/// Filters stations based on the selected city.
class CitySelector extends ConsumerWidget {
  const CitySelector({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final citiesAsync = ref.watch(citiesProvider);
    final selectedCity = ref.watch(selectedCityProvider);

    return citiesAsync.when(
      data: (cities) {
        if (cities.isEmpty) {
          return const SizedBox.shrink();
        }

        return DropdownButton<String?>(
          value: selectedCity,
          hint: const Text(
            '\u0627\u062E\u062A\u0631 \u0627\u0644\u0645\u062F\u064A\u0646\u0629',
            style: TextStyle(color: Colors.white70, fontSize: 14),
          ),
          underline: const SizedBox.shrink(),
          icon: const Icon(Icons.arrow_drop_down, color: Colors.white),
          dropdownColor: Theme.of(context).primaryColor,
          style: const TextStyle(color: Colors.white, fontSize: 14),
          items: [
            const DropdownMenuItem<String?>(
              value: null,
              child: Text(
                '\u0627\u0644\u0643\u0644',
                style: TextStyle(color: Colors.white),
              ),
            ),
            ...cities.map((city) => DropdownMenuItem<String?>(
                  value: city.id,
                  child: Text(
                    city.nameAr,
                    style: const TextStyle(color: Colors.white),
                  ),
                )),
          ],
          onChanged: (value) {
            ref.read(selectedCityProvider.notifier).state = value;
          },
        );
      },
      loading: () => const SizedBox(
        width: 20,
        height: 20,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          color: Colors.white,
        ),
      ),
      error: (_, __) => const SizedBox.shrink(),
    );
  }
}

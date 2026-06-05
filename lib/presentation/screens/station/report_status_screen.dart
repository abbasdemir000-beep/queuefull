import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/constants/app_constants.dart';
import '../../providers/location_provider.dart';
import '../../providers/station_provider.dart';
import '../../providers/user_provider.dart';
import '../../widgets/gps_indicator.dart';
import '../../widgets/queue_status_chip.dart';

/// Screen for reporting station queue status.
class ReportStatusScreen extends ConsumerStatefulWidget {
  const ReportStatusScreen({super.key, required this.stationId});

  final String stationId;

  @override
  ConsumerState<ReportStatusScreen> createState() => _ReportStatusScreenState();
}

class _ReportStatusScreenState extends ConsumerState<ReportStatusScreen> {
  QueueStatus _selectedStatus = QueueStatus.empty;
  bool _hasFuel = true;
  FuelType _selectedFuelType = FuelType.regular;
  bool _isSubmitting = false;

  Future<void> _submitReport() async {
    final currentUser = ref.read(currentUserProvider);
    if (currentUser == null) return;

    final locationAsync = ref.read(currentLocationProvider);
    final position = locationAsync.valueOrNull;
    if (position == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('\u062A\u0639\u0630\u0631 \u062A\u062D\u062F\u064A\u062F \u0627\u0644\u0645\u0648\u0642\u0639'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    setState(() => _isSubmitting = true);

    try {
      await ref.read(stationActionsProvider.notifier).reportStatus(
            stationId: widget.stationId,
            status: _selectedStatus,
            hasFuel: _hasFuel,
            fuelType: _selectedFuelType,
            userPhone: currentUser.phoneNumber,
            latitude: position.latitude,
            longitude: position.longitude,
          );

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            '\u062A\u0645 \u0627\u0644\u0625\u0628\u0644\u0627\u063A \u0628\u0646\u062C\u0627\u062D! +15 \u0646\u0642\u0637\u0629',
          ),
          backgroundColor: Colors.green,
        ),
      );
      context.pop();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('\u062E\u0637\u0623: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final stationAsync = ref.watch(stationProvider(widget.stationId));

    return Scaffold(
      appBar: AppBar(
        title: const Text('\u0625\u0628\u0644\u0627\u063A \u062D\u0627\u0644\u0629 \u0627\u0644\u0645\u062D\u0637\u0629'),
      ),
      body: stationAsync.when(
        data: (station) {
          if (station == null) {
            return const Center(
              child: Text('\u0627\u0644\u0645\u062D\u0637\u0629 \u063A\u064A\u0631 \u0645\u0648\u062C\u0648\u062F\u0629'),
            );
          }

          final isWithinRange = ref.watch(
            proximityProvider(
                (lat: station.latitude, lng: station.longitude)),
          );

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Station name
                Text(
                  station.name,
                  style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 20),

                // Queue status selector
                const Text(
                  '\u062D\u0627\u0644\u0629 \u0627\u0644\u0637\u0627\u0628\u0648\u0631:',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: QueueStatus.values.map((status) {
                    return QueueStatusChip(
                      status: status,
                      large: true,
                      selected: _selectedStatus == status,
                      onTap: () => setState(() => _selectedStatus = status),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 24),

                // Fuel availability toggle
                Card(
                  child: SwitchListTile(
                    title: const Text('\u0627\u0644\u0648\u0642\u0648\u062F \u0645\u062A\u0648\u0641\u0631'),
                    value: _hasFuel,
                    onChanged: (val) => setState(() => _hasFuel = val),
                    secondary: Icon(
                      _hasFuel ? Icons.local_gas_station : Icons.block,
                      color: _hasFuel ? Colors.green : Colors.red,
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // Fuel type dropdown
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Row(
                      children: [
                        const Text(
                          '\u0646\u0648\u0639 \u0627\u0644\u0648\u0642\u0648\u062F:',
                          style: TextStyle(fontSize: 16),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: DropdownButton<FuelType>(
                            value: _selectedFuelType,
                            isExpanded: true,
                            items: FuelType.values
                                .map((ft) => DropdownMenuItem(
                                      value: ft,
                                      child: Text(ft.displayName),
                                    ))
                                .toList(),
                            onChanged: (val) {
                              if (val != null) {
                                setState(() => _selectedFuelType = val);
                              }
                            },
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),

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

                // Submit button
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton(
                    onPressed:
                        (isWithinRange && !_isSubmitting) ? _submitReport : null,
                    child: _isSubmitting
                        ? const SizedBox(
                            width: 24,
                            height: 24,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Colors.white,
                            ),
                          )
                        : const Text(
                            '\u0625\u0631\u0633\u0627\u0644 \u0627\u0644\u0625\u0628\u0644\u0627\u063A',
                            style: TextStyle(fontSize: 16),
                          ),
                  ),
                ),
                if (!isWithinRange) ...[
                  const SizedBox(height: 8),
                  const Center(
                    child: Text(
                      '\u064A\u062C\u0628 \u0623\u0646 \u062A\u0643\u0648\u0646 \u0636\u0645\u0646 200 \u0645\u062A\u0631 \u0645\u0646 \u0627\u0644\u0645\u062D\u0637\u0629 \u0644\u0644\u0625\u0628\u0644\u0627\u063A',
                      style: TextStyle(color: Colors.orange, fontSize: 12),
                    ),
                  ),
                ],
              ],
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('\u062E\u0637\u0623: $e')),
      ),
    );
  }
}

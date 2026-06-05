import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/feature_flag_provider.dart';

/// Conditional ad banner widget.
/// Only renders if ADS_ENABLED feature flag is true.
/// Currently displays a placeholder for future ad integration.
class AdBannerWidget extends ConsumerWidget {
  const AdBannerWidget({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final adsEnabled = ref.watch(adsEnabledProvider);

    if (!adsEnabled) {
      return const SizedBox.shrink();
    }

    return Container(
      width: double.infinity,
      height: 60,
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.grey[200],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey[300]!),
      ),
      child: const Center(
        child: Text(
          '\u0625\u0639\u0644\u0627\u0646',
          style: TextStyle(color: Colors.grey, fontSize: 14),
        ),
      ),
    );
  }
}

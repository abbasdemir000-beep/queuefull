import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/config/feature_flags.dart';

/// Simple providers exposing FeatureFlags constants for conditional UI rendering.
/// These can be used to conditionally show/hide features based on the current config.

/// Whether the app is in free mode (no subscription required).
final freeModeProvider = Provider<bool>((ref) => FeatureFlags.freeMode);

/// Whether ads are enabled.
final adsEnabledProvider = Provider<bool>((ref) => FeatureFlags.adsEnabled);

/// Whether subscription is required for premium features.
final subscriptionRequiredProvider = Provider<bool>(
  (ref) => FeatureFlags.subscriptionRequired,
);

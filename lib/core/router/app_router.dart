import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

/// Route path constants
class AppRoutes {
  AppRoutes._();

  static const String splash = '/';
  static const String auth = '/auth';
  static const String authOtp = '/auth/otp';
  static const String home = '/home';
  static const String map = '/map';
  static const String stations = '/stations';
  static const String leaderboard = '/leaderboard';
  static const String profile = '/profile';
  static const String stationDetail = '/station/:id';
  static const String stationReport = '/station/:id/report';
  static const String notifications = '/notifications';
  static const String admin = '/admin';
  static const String adminStations = '/admin/stations';
  static const String adminCities = '/admin/cities';
  static const String adminUsers = '/admin/users';
  static const String adminSuggestions = '/admin/suggestions';
}

/// Provider for auth state used by the router redirect logic.
/// This will be overridden in the actual app with real auth state.
final authStateProvider = StateProvider<bool>((ref) => false);

/// GoRouter provider
final routerProvider = Provider<GoRouter>((ref) {
  final isLoggedIn = ref.watch(authStateProvider);

  return GoRouter(
    initialLocation: AppRoutes.splash,
    redirect: (BuildContext context, GoRouterState state) {
      final isOnAuth = state.matchedLocation == AppRoutes.auth ||
          state.matchedLocation == AppRoutes.authOtp;
      final isOnSplash = state.matchedLocation == AppRoutes.splash;

      // Allow splash to show regardless
      if (isOnSplash) return null;

      // Not logged in - redirect to auth (unless already there)
      if (!isLoggedIn && !isOnAuth) {
        return AppRoutes.auth;
      }

      // Logged in but on auth page - redirect to home
      if (isLoggedIn && isOnAuth) {
        return AppRoutes.home;
      }

      return null;
    },
    routes: [
      GoRoute(
        path: AppRoutes.splash,
        name: 'splash',
        builder: (context, state) => const _PlaceholderScreen(title: 'Splash'),
      ),
      GoRoute(
        path: AppRoutes.auth,
        name: 'auth',
        builder: (context, state) => const _PlaceholderScreen(title: 'Auth'),
      ),
      GoRoute(
        path: AppRoutes.authOtp,
        name: 'authOtp',
        builder: (context, state) =>
            const _PlaceholderScreen(title: 'OTP Verification'),
      ),
      ShellRoute(
        builder: (context, state, child) => _ShellScaffold(child: child),
        routes: [
          GoRoute(
            path: AppRoutes.home,
            name: 'home',
            builder: (context, state) =>
                const _PlaceholderScreen(title: 'Home'),
          ),
          GoRoute(
            path: AppRoutes.map,
            name: 'map',
            builder: (context, state) => const _PlaceholderScreen(title: 'Map'),
          ),
          GoRoute(
            path: AppRoutes.stations,
            name: 'stations',
            builder: (context, state) =>
                const _PlaceholderScreen(title: 'Stations'),
          ),
          GoRoute(
            path: AppRoutes.leaderboard,
            name: 'leaderboard',
            builder: (context, state) =>
                const _PlaceholderScreen(title: 'Leaderboard'),
          ),
          GoRoute(
            path: AppRoutes.profile,
            name: 'profile',
            builder: (context, state) =>
                const _PlaceholderScreen(title: 'Profile'),
          ),
        ],
      ),
      GoRoute(
        path: AppRoutes.stationDetail,
        name: 'stationDetail',
        builder: (context, state) {
          final id = state.pathParameters['id'] ?? '';
          return _PlaceholderScreen(title: 'Station $id');
        },
      ),
      GoRoute(
        path: AppRoutes.stationReport,
        name: 'stationReport',
        builder: (context, state) {
          final id = state.pathParameters['id'] ?? '';
          return _PlaceholderScreen(title: 'Report for $id');
        },
      ),
      GoRoute(
        path: AppRoutes.notifications,
        name: 'notifications',
        builder: (context, state) =>
            const _PlaceholderScreen(title: 'Notifications'),
      ),
      GoRoute(
        path: AppRoutes.admin,
        name: 'admin',
        builder: (context, state) =>
            const _PlaceholderScreen(title: 'Admin Panel'),
      ),
      GoRoute(
        path: AppRoutes.adminStations,
        name: 'adminStations',
        builder: (context, state) =>
            const _PlaceholderScreen(title: 'Manage Stations'),
      ),
      GoRoute(
        path: AppRoutes.adminCities,
        name: 'adminCities',
        builder: (context, state) =>
            const _PlaceholderScreen(title: 'Manage Cities'),
      ),
      GoRoute(
        path: AppRoutes.adminUsers,
        name: 'adminUsers',
        builder: (context, state) =>
            const _PlaceholderScreen(title: 'Manage Users'),
      ),
      GoRoute(
        path: AppRoutes.adminSuggestions,
        name: 'adminSuggestions',
        builder: (context, state) =>
            const _PlaceholderScreen(title: 'Pending Suggestions'),
      ),
    ],
  );
});

/// Placeholder screen used until real screens are implemented
class _PlaceholderScreen extends StatelessWidget {
  const _PlaceholderScreen({required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Center(child: Text(title)),
    );
  }
}

/// Shell scaffold with bottom navigation for main tabs
class _ShellScaffold extends StatelessWidget {
  const _ShellScaffold({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: child,
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _calculateIndex(GoRouterState.of(context).matchedLocation),
        onTap: (index) => _onTap(context, index),
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.map), label: 'Map'),
          BottomNavigationBarItem(
              icon: Icon(Icons.local_gas_station), label: 'Stations'),
          BottomNavigationBarItem(
              icon: Icon(Icons.leaderboard), label: 'Leaderboard'),
          BottomNavigationBarItem(icon: Icon(Icons.person), label: 'Profile'),
        ],
      ),
    );
  }

  int _calculateIndex(String location) {
    if (location.startsWith(AppRoutes.map)) return 1;
    if (location.startsWith(AppRoutes.stations)) return 2;
    if (location.startsWith(AppRoutes.leaderboard)) return 3;
    if (location.startsWith(AppRoutes.profile)) return 4;
    return 0;
  }

  void _onTap(BuildContext context, int index) {
    switch (index) {
      case 0:
        context.go(AppRoutes.home);
        break;
      case 1:
        context.go(AppRoutes.map);
        break;
      case 2:
        context.go(AppRoutes.stations);
        break;
      case 3:
        context.go(AppRoutes.leaderboard);
        break;
      case 4:
        context.go(AppRoutes.profile);
        break;
    }
  }
}

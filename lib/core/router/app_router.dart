import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/constants/app_constants.dart';
import '../../presentation/providers/auth_provider.dart';
import '../../presentation/screens/admin/admin_cities_screen.dart';
import '../../presentation/screens/admin/admin_panel_screen.dart';
import '../../presentation/screens/admin/admin_stations_screen.dart';
import '../../presentation/screens/admin/admin_suggestions_screen.dart';
import '../../presentation/screens/admin/admin_users_screen.dart';
import '../../presentation/screens/auth/otp_verification_screen.dart';
import '../../presentation/screens/auth/phone_input_screen.dart';
import '../../presentation/screens/home/home_screen.dart';
import '../../presentation/screens/notifications/notification_screen.dart';
import '../../presentation/screens/splash/splash_screen.dart';
import '../../presentation/screens/station/report_status_screen.dart';
import '../../presentation/screens/station/station_detail_screen.dart';

/// Route path constants
class AppRoutes {
  AppRoutes._();

  static const String splash = '/';
  static const String auth = '/auth';
  static const String authOtp = '/auth/otp';
  static const String home = '/home';
  static const String stationDetail = '/station/:id';
  static const String stationReport = '/station/:id/report';
  static const String notifications = '/notifications';
  static const String admin = '/admin';
  static const String adminStations = '/admin/stations';
  static const String adminCities = '/admin/cities';
  static const String adminUsers = '/admin/users';
  static const String adminSuggestions = '/admin/suggestions';
}

/// GoRouter provider
final routerProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authStateProvider);

  return GoRouter(
    initialLocation: AppRoutes.splash,
    redirect: (BuildContext context, GoRouterState state) {
      final isLoading = authState.isLoading;
      final currentUser = authState.valueOrNull;
      final isLoggedIn = currentUser != null;
      final isOnAuth = state.matchedLocation == AppRoutes.auth ||
          state.matchedLocation == AppRoutes.authOtp;
      final isOnSplash = state.matchedLocation == AppRoutes.splash;

      // While auth state is loading, allow splash to show
      if (isLoading && isOnSplash) return null;

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

      // Admin route protection: only admin role can access /admin/* paths
      final isAdminRoute = state.matchedLocation.startsWith('/admin');
      if (isAdminRoute && isLoggedIn && currentUser.role != UserRole.admin) {
        return AppRoutes.home;
      }

      return null;
    },
    routes: [
      GoRoute(
        path: AppRoutes.splash,
        name: 'splash',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: AppRoutes.auth,
        name: 'auth',
        builder: (context, state) => const PhoneInputScreen(),
      ),
      GoRoute(
        path: AppRoutes.authOtp,
        name: 'authOtp',
        builder: (context, state) {
          final extra = state.extra as Map<String, String>?;
          return OtpVerificationScreen(
            phoneNumber: extra?['phone'] ?? '',
            name: extra?['name'] ?? '',
          );
        },
      ),
      GoRoute(
        path: AppRoutes.home,
        name: 'home',
        builder: (context, state) => const HomeScreen(),
      ),
      GoRoute(
        path: AppRoutes.stationDetail,
        name: 'stationDetail',
        builder: (context, state) {
          final id = state.pathParameters['id'] ?? '';
          return StationDetailScreen(stationId: id);
        },
      ),
      GoRoute(
        path: AppRoutes.stationReport,
        name: 'stationReport',
        builder: (context, state) {
          final id = state.pathParameters['id'] ?? '';
          return ReportStatusScreen(stationId: id);
        },
      ),
      GoRoute(
        path: AppRoutes.notifications,
        name: 'notifications',
        builder: (context, state) => const NotificationScreen(),
      ),
      GoRoute(
        path: AppRoutes.admin,
        name: 'admin',
        builder: (context, state) => const AdminPanelScreen(),
      ),
      GoRoute(
        path: AppRoutes.adminStations,
        name: 'adminStations',
        builder: (context, state) => const AdminStationsScreen(),
      ),
      GoRoute(
        path: AppRoutes.adminCities,
        name: 'adminCities',
        builder: (context, state) => const AdminCitiesScreen(),
      ),
      GoRoute(
        path: AppRoutes.adminUsers,
        name: 'adminUsers',
        builder: (context, state) => const AdminUsersScreen(),
      ),
      GoRoute(
        path: AppRoutes.adminSuggestions,
        name: 'adminSuggestions',
        builder: (context, state) => const AdminSuggestionsScreen(),
      ),
    ],
  );
});

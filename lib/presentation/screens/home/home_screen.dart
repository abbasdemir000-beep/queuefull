import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../providers/notification_provider.dart';
import '../../widgets/city_selector.dart';
import '../leaderboard/leaderboard_screen.dart';
import '../map/map_screen.dart';
import '../profile/profile_screen.dart';
import '../station/station_list_screen.dart';

/// Home screen with bottom navigation and city selector.
class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  int _currentIndex = 0;

  final List<Widget> _tabs = const [
    MapScreen(),
    StationListScreen(),
    LeaderboardScreen(),
    ProfileScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    final unreadCount = ref.watch(unreadCountProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('QueueFuel'),
        actions: [
          const CitySelector(),
          const SizedBox(width: 8),
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.notifications),
                onPressed: () => context.push('/notifications'),
              ),
              if (unreadCount > 0)
                Positioned(
                  right: 6,
                  top: 6,
                  child: Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(
                      color: Colors.red,
                      shape: BoxShape.circle,
                    ),
                    constraints: const BoxConstraints(
                      minWidth: 18,
                      minHeight: 18,
                    ),
                    child: Text(
                      '$unreadCount',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                      textAlign: TextAlign.center,
                    ),
                  ),
                ),
            ],
          ),
        ],
      ),
      body: IndexedStack(
        index: _currentIndex,
        children: _tabs,
      ),
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _currentIndex,
        onTap: (index) => setState(() => _currentIndex = index),
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.map),
            label: '\u0627\u0644\u062E\u0631\u064A\u0637\u0629',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.list),
            label: '\u0627\u0644\u0645\u062D\u0637\u0627\u062A',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.leaderboard),
            label: '\u0627\u0644\u062A\u0631\u062A\u064A\u0628',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person),
            label: '\u0627\u0644\u0645\u0644\u0641',
          ),
        ],
      ),
    );
  }
}

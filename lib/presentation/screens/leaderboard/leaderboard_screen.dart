import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../providers/cycle_provider.dart';
import '../../providers/user_provider.dart';
import '../../widgets/cycle_timer_widget.dart';

/// Leaderboard screen showing ranked users by points.
class LeaderboardScreen extends ConsumerWidget {
  const LeaderboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final leaderboardAsync = ref.watch(leaderboardProvider);
    final currentUser = ref.watch(currentUserProvider);

    return Column(
      children: [
        // Cycle timer at top
        const Padding(
          padding: EdgeInsets.all(16),
          child: CycleTimerWidget(),
        ),
        const Divider(height: 1),

        // Leaderboard list
        Expanded(
          child: leaderboardAsync.when(
            data: (entries) {
              if (entries.isEmpty) {
                return Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.leaderboard, size: 64, color: Colors.grey[400]),
                      const SizedBox(height: 16),
                      Text(
                        '\u0644\u0627 \u062A\u0648\u062C\u062F \u0628\u064A\u0627\u0646\u0627\u062A \u0644\u0647\u0630\u0647 \u0627\u0644\u062F\u0648\u0631\u0629',
                        style: TextStyle(
                          fontSize: 16,
                          color: Colors.grey[600],
                        ),
                      ),
                    ],
                  ),
                );
              }

              return ListView.builder(
                padding: const EdgeInsets.symmetric(vertical: 8),
                itemCount: entries.length,
                itemBuilder: (context, index) {
                  final entry = entries[index];
                  final isCurrentUser =
                      currentUser != null &&
                      entry.userPhone == currentUser.phoneNumber;
                  final isTopThree = entry.rank <= 3;

                  return Container(
                    margin: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: isCurrentUser
                          ? Colors.teal.withOpacity(0.1)
                          : null,
                      borderRadius: BorderRadius.circular(8),
                      border: isCurrentUser
                          ? Border.all(color: Colors.teal, width: 1.5)
                          : null,
                    ),
                    child: ListTile(
                      leading: _buildRankWidget(entry.rank),
                      title: Text(
                        entry.userName,
                        style: TextStyle(
                          fontWeight: isTopThree
                              ? FontWeight.bold
                              : FontWeight.normal,
                        ),
                      ),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(
                            Icons.star,
                            color: Colors.amber,
                            size: 18,
                          ),
                          const SizedBox(width: 4),
                          Text(
                            '${entry.points}',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: isTopThree
                                  ? FontWeight.bold
                                  : FontWeight.normal,
                            ),
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
        ),
      ],
    );
  }

  Widget _buildRankWidget(int rank) {
    switch (rank) {
      case 1:
        return const CircleAvatar(
          backgroundColor: Colors.amber,
          child: Icon(Icons.emoji_events, color: Colors.white),
        );
      case 2:
        return CircleAvatar(
          backgroundColor: Colors.grey[400],
          child: const Icon(Icons.emoji_events, color: Colors.white),
        );
      case 3:
        return CircleAvatar(
          backgroundColor: Colors.brown[300],
          child: const Icon(Icons.emoji_events, color: Colors.white),
        );
      default:
        return CircleAvatar(
          backgroundColor: Colors.grey[200],
          child: Text(
            '$rank',
            style: const TextStyle(
              fontWeight: FontWeight.bold,
              color: Colors.black87,
            ),
          ),
        );
    }
  }
}

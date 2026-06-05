import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../domain/models/app_user.dart';
import '../../providers/repository_providers.dart';

/// Provider watching all users for admin management.
final _allUsersProvider = StreamProvider<List<AppUser>>((ref) {
  final userRepo = ref.watch(userRepositoryProvider);
  return userRepo.watchAllUsers();
});

/// Admin screen for managing users.
class AdminUsersScreen extends ConsumerWidget {
  const AdminUsersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final usersAsync = ref.watch(_allUsersProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('\u0625\u062F\u0627\u0631\u0629 \u0627\u0644\u0645\u0633\u062A\u062E\u062F\u0645\u064A\u0646'),
      ),
      body: usersAsync.when(
        data: (users) {
          if (users.isEmpty) {
            return const Center(
              child: Text('\u0644\u0627 \u064A\u0648\u062C\u062F \u0645\u0633\u062A\u062E\u062F\u0645\u0648\u0646'),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.symmetric(vertical: 8),
            itemCount: users.length,
            itemBuilder: (context, index) {
              final user = users[index];
              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: user.banned
                        ? Colors.red[100]
                        : Colors.teal[100],
                    child: Text(
                      user.name.isNotEmpty ? user.name[0].toUpperCase() : '?',
                      style: TextStyle(
                        color: user.banned ? Colors.red : Colors.teal,
                      ),
                    ),
                  ),
                  title: Row(
                    children: [
                      Expanded(child: Text(user.name)),
                      if (user.banned)
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 6,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.red[50],
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text(
                            '\u0645\u062D\u0638\u0648\u0631',
                            style: TextStyle(
                              color: Colors.red,
                              fontSize: 11,
                            ),
                          ),
                        ),
                    ],
                  ),
                  subtitle: Row(
                    children: [
                      Text(user.phoneNumber),
                      const SizedBox(width: 8),
                      Text(
                        user.role.displayName,
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey[600],
                        ),
                      ),
                      const Spacer(),
                      const Icon(Icons.star, size: 14, color: Colors.amber),
                      Text(
                        ' ${user.points}',
                        style: const TextStyle(fontSize: 12),
                      ),
                    ],
                  ),
                  trailing: IconButton(
                    icon: Icon(
                      user.banned ? Icons.lock_open : Icons.block,
                      color: user.banned ? Colors.green : Colors.red,
                    ),
                    onPressed: () =>
                        _showBanDialog(context, ref, user),
                  ),
                ),
              );
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('\u062E\u0637\u0623: $e')),
      ),
    );
  }

  void _showBanDialog(BuildContext context, WidgetRef ref, AppUser user) {
    final isBanning = !user.banned;
    final reasonController = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(isBanning
            ? '\u062D\u0638\u0631 \u0627\u0644\u0645\u0633\u062A\u062E\u062F\u0645'
            : '\u0625\u0644\u063A\u0627\u0621 \u0627\u0644\u062D\u0638\u0631'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(isBanning
                ? '\u0647\u0644 \u0623\u0646\u062A \u0645\u062A\u0623\u0643\u062F \u0645\u0646 \u062D\u0638\u0631 "${user.name}"\u061F'
                : '\u0647\u0644 \u062A\u0631\u064A\u062F \u0625\u0644\u063A\u0627\u0621 \u062D\u0638\u0631 "${user.name}"\u061F'),
            if (isBanning) ...[
              const SizedBox(height: 12),
              TextField(
                controller: reasonController,
                decoration: const InputDecoration(
                  labelText: '\u0627\u0644\u0633\u0628\u0628',
                  border: OutlineInputBorder(),
                ),
              ),
            ],
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('\u0625\u0644\u063A\u0627\u0621'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(ctx);
              final userRepo = ref.read(userRepositoryProvider);
              await userRepo.updateUser(
                user.copyWith(banned: isBanning),
              );
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: isBanning ? Colors.red : Colors.green,
            ),
            child: Text(isBanning ? '\u062D\u0638\u0631' : '\u0625\u0644\u063A\u0627\u0621 \u0627\u0644\u062D\u0638\u0631'),
          ),
        ],
      ),
    );
  }
}

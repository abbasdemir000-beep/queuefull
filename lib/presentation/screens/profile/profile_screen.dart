import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/models/app_user.dart';
import '../../providers/auth_provider.dart';
import '../../providers/user_provider.dart';

/// Profile screen showing user info and actions.
class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(currentUserProvider);

    if (user == null) {
      return const Center(child: CircularProgressIndicator());
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          const SizedBox(height: 24),
          // Avatar
          CircleAvatar(
            radius: 48,
            backgroundColor: Theme.of(context).primaryColor,
            child: Text(
              user.name.isNotEmpty ? user.name[0].toUpperCase() : '?',
              style: const TextStyle(
                fontSize: 36,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Name
          Text(
            user.name,
            style: const TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 4),

          // Phone
          Text(
            user.phoneNumber,
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey[600],
            ),
            textDirection: TextDirection.ltr,
          ),
          const SizedBox(height: 12),

          // Role badge
          _RoleBadge(role: user.role),
          const SizedBox(height: 16),

          // Points
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.star, color: Colors.amber, size: 28),
                  const SizedBox(width: 8),
                  Text(
                    '${user.points}',
                    style: const TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(width: 4),
                  const Text(
                    '\u0646\u0642\u0637\u0629',
                    style: TextStyle(fontSize: 16),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // Admin panel button
          if (user.role == UserRole.admin) ...[
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () => context.push('/admin'),
                icon: const Icon(Icons.admin_panel_settings),
                label: const Text('\u0644\u0648\u062D\u0629 \u0627\u0644\u0625\u062F\u0627\u0631\u0629'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.purple,
                  foregroundColor: Colors.white,
                ),
              ),
            ),
            const SizedBox(height: 16),
          ],

          // Actions
          const Divider(),
          const SizedBox(height: 8),

          // Edit name
          ListTile(
            leading: const Icon(Icons.edit),
            title: const Text('\u062A\u0639\u062F\u064A\u0644 \u0627\u0644\u0627\u0633\u0645'),
            onTap: () => _showEditNameDialog(context, ref, user),
          ),

          // Sign out
          ListTile(
            leading: const Icon(Icons.logout, color: Colors.orange),
            title: const Text(
              '\u062A\u0633\u062C\u064A\u0644 \u0627\u0644\u062E\u0631\u0648\u062C',
              style: TextStyle(color: Colors.orange),
            ),
            onTap: () => _showSignOutDialog(context, ref),
          ),

          // Delete account
          ListTile(
            leading: const Icon(Icons.delete_forever, color: Colors.red),
            title: const Text(
              '\u062D\u0630\u0641 \u0627\u0644\u062D\u0633\u0627\u0628',
              style: TextStyle(color: Colors.red),
            ),
            onTap: () => _showDeleteAccountDialog(context, ref),
          ),
        ],
      ),
    );
  }

  void _showEditNameDialog(BuildContext context, WidgetRef ref, AppUser user) {
    final controller = TextEditingController(text: user.name);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('\u062A\u0639\u062F\u064A\u0644 \u0627\u0644\u0627\u0633\u0645'),
        content: TextField(
          controller: controller,
          decoration: const InputDecoration(
            labelText: '\u0627\u0644\u0627\u0633\u0645 \u0627\u0644\u062C\u062F\u064A\u062F',
            border: OutlineInputBorder(),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('\u0625\u0644\u063A\u0627\u0621'),
          ),
          ElevatedButton(
            onPressed: () {
              final newName = controller.text.trim();
              if (newName.isNotEmpty) {
                ref.read(userActionsProvider.notifier).updateProfile(
                      user.copyWith(name: newName),
                    );
                Navigator.pop(ctx);
              }
            },
            child: const Text('\u062D\u0641\u0638'),
          ),
        ],
      ),
    );
  }

  void _showSignOutDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('\u062A\u0633\u062C\u064A\u0644 \u0627\u0644\u062E\u0631\u0648\u062C'),
        content: const Text('\u0647\u0644 \u0623\u0646\u062A \u0645\u062A\u0623\u0643\u062F \u0645\u0646 \u062A\u0633\u062C\u064A\u0644 \u0627\u0644\u062E\u0631\u0648\u062C\u061F'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('\u0625\u0644\u063A\u0627\u0621'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(ctx);
              ref.read(phoneVerificationProvider.notifier).reset();
              context.go('/auth');
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.orange),
            child: const Text('\u062E\u0631\u0648\u062C'),
          ),
        ],
      ),
    );
  }

  void _showDeleteAccountDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('\u062D\u0630\u0641 \u0627\u0644\u062D\u0633\u0627\u0628'),
        content: const Text(
          '\u0647\u0630\u0627 \u0627\u0644\u0625\u062C\u0631\u0627\u0621 \u0644\u0627 \u064A\u0645\u0643\u0646 \u0627\u0644\u062A\u0631\u0627\u062C\u0639 \u0639\u0646\u0647. \u0633\u064A\u062A\u0645 \u062D\u0630\u0641 \u062C\u0645\u064A\u0639 \u0628\u064A\u0627\u0646\u0627\u062A\u0643.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('\u0625\u0644\u063A\u0627\u0621'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(ctx);
              await ref.read(userActionsProvider.notifier).deleteAccount();
              if (context.mounted) {
                context.go('/auth');
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('\u062D\u0630\u0641'),
          ),
        ],
      ),
    );
  }
}

class _RoleBadge extends StatelessWidget {
  const _RoleBadge({required this.role});

  final UserRole role;

  Color get _color {
    switch (role) {
      case UserRole.user:
        return Colors.blue;
      case UserRole.reporter:
        return Colors.green;
      case UserRole.admin:
        return Colors.purple;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Chip(
      label: Text(
        role.displayName,
        style: const TextStyle(color: Colors.white),
      ),
      backgroundColor: _color,
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

/// Admin panel screen with navigation to management areas.
class AdminPanelScreen extends ConsumerWidget {
  const AdminPanelScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('\u0644\u0648\u062D\u0629 \u0627\u0644\u0625\u062F\u0627\u0631\u0629'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: GridView.count(
          crossAxisCount: 2,
          mainAxisSpacing: 16,
          crossAxisSpacing: 16,
          children: [
            _AdminCard(
              icon: Icons.local_gas_station,
              title: '\u0625\u062F\u0627\u0631\u0629 \u0627\u0644\u0645\u062D\u0637\u0627\u062A',
              description: '\u0625\u0636\u0627\u0641\u0629 \u0648\u062A\u0639\u062F\u064A\u0644 \u0648\u062D\u0630\u0641 \u0627\u0644\u0645\u062D\u0637\u0627\u062A',
              color: Colors.blue,
              onTap: () => context.push('/admin/stations'),
            ),
            _AdminCard(
              icon: Icons.location_city,
              title: '\u0625\u062F\u0627\u0631\u0629 \u0627\u0644\u0645\u062F\u0646',
              description: '\u0625\u0636\u0627\u0641\u0629 \u0648\u0625\u062F\u0627\u0631\u0629 \u0627\u0644\u0645\u062F\u0646',
              color: Colors.green,
              onTap: () => context.push('/admin/cities'),
            ),
            _AdminCard(
              icon: Icons.people,
              title: '\u0625\u062F\u0627\u0631\u0629 \u0627\u0644\u0645\u0633\u062A\u062E\u062F\u0645\u064A\u0646',
              description: '\u062D\u0638\u0631 \u0648\u0625\u0644\u063A\u0627\u0621 \u062D\u0638\u0631 \u0627\u0644\u0645\u0633\u062A\u062E\u062F\u0645\u064A\u0646',
              color: Colors.orange,
              onTap: () => context.push('/admin/users'),
            ),
            _AdminCard(
              icon: Icons.pending_actions,
              title: '\u0627\u0644\u0627\u0642\u062A\u0631\u0627\u062D\u0627\u062A',
              description: '\u0645\u0631\u0627\u062C\u0639\u0629 \u0627\u0644\u0627\u0642\u062A\u0631\u0627\u062D\u0627\u062A \u0627\u0644\u0645\u0639\u0644\u0642\u0629',
              color: Colors.purple,
              onTap: () => context.push('/admin/suggestions'),
            ),
          ],
        ),
      ),
    );
  }
}

class _AdminCard extends StatelessWidget {
  const _AdminCard({
    required this.icon,
    required this.title,
    required this.description,
    required this.color,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String description;
  final Color color;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, size: 40, color: color),
              const SizedBox(height: 12),
              Text(
                title,
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 4),
              Text(
                description,
                style: TextStyle(
                  fontSize: 11,
                  color: Colors.grey[600],
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

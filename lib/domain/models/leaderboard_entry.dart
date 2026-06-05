/// Domain model representing a leaderboard entry.
class LeaderboardEntry {
  const LeaderboardEntry({
    required this.userPhone,
    required this.userName,
    required this.points,
    required this.rank,
  });

  final String userPhone;
  final String userName;
  final int points;
  final int rank;

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is LeaderboardEntry &&
        other.userPhone == userPhone &&
        other.rank == rank;
  }

  @override
  int get hashCode => Object.hash(userPhone, rank);

  @override
  String toString() =>
      'LeaderboardEntry(userName: $userName, points: $points, rank: $rank)';
}

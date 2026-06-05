/// Points policy ported from PointsPolicy.kt.
/// Defines point values for different user actions in the app.
class PointsPolicy {
  PointsPolicy._();

  /// Points awarded for reporting a station status
  static const int reportPoints = 15;

  /// Points awarded for confirming another user's report
  static const int confirmationPoints = 5;

  /// Points awarded for suggesting a new station
  static const int stationSuggestionPoints = 30;

  /// Points awarded when a suggested station is approved by admin
  static const int stationApprovalPoints = 50;

  /// Points awarded to new users on registration
  static const int welcomePoints = 20;

  /// Points awarded at the start of each new cycle
  static const int cycleResetPoints = 20;

  /// Initial points for admin accounts
  static const int adminInitialPoints = 250;

  /// Award the appropriate points for a given action type.
  /// Returns the number of points to award.
  static int award(PointAction action) {
    switch (action) {
      case PointAction.report:
        return reportPoints;
      case PointAction.confirmation:
        return confirmationPoints;
      case PointAction.stationSuggestion:
        return stationSuggestionPoints;
      case PointAction.stationApproval:
        return stationApprovalPoints;
      case PointAction.welcome:
        return welcomePoints;
      case PointAction.cycleReset:
        return cycleResetPoints;
      case PointAction.adminInitial:
        return adminInitialPoints;
    }
  }
}

/// Actions that can earn points
enum PointAction {
  report,
  confirmation,
  stationSuggestion,
  stationApproval,
  welcome,
  cycleReset,
  adminInitial,
}

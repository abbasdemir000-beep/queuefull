package com.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.ui.QueueFuelViewModel
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.PlaceDetailsScreen
import com.example.ui.screens.ReportWizardScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Full-screen captures of the redesigned surfaces, rendered RTL against the
 * real ViewModel + seeded Room database (Robolectric). CI uploads
 * src/test/screenshots/ as the "ui-screenshots" artifact for visual review.
 *
 * All screens are captured in ONE test on purpose: the Room singleton and the
 * ViewModel's long-lived coroutines do not survive being torn down between
 * Robolectric test methods (closing the DB mid-flight leaks uncaught
 * exceptions into the next test), and a compose rule allows only a single
 * setContent — so we switch screens through a state variable instead.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ScreenScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  private enum class Screen { HOME, MAP, DETAILS, WIZARD, REWARDS, ACCOUNT }

  /** The app renders RTL under the Arabic locale; force it for captures. */
  @Composable
  private fun Rtl(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      MyApplicationTheme { content() }
    }
  }

  @Test
  fun all_screens_screenshots() {
    val viewModel = QueueFuelViewModel(ApplicationProvider.getApplicationContext())
    var screen by mutableStateOf(Screen.HOME)

    composeTestRule.setContent {
      val stations by viewModel.approvedStations.collectAsState()
      Rtl {
        when (screen) {
          Screen.HOME -> HomeScreen(viewModel = viewModel, onOpenAlerts = {}, onStationClick = {})
          Screen.MAP -> MapScreen(viewModel = viewModel, onOpenDetails = {})
          Screen.DETAILS -> stations.firstOrNull()?.let { station ->
            PlaceDetailsScreen(station = station, viewModel = viewModel, onBack = {}, onNewReport = {})
          }
          Screen.WIZARD -> ReportWizardScreen(viewModel = viewModel, initialStation = null, onClose = {})
          Screen.REWARDS -> RewardsScreen(viewModel = viewModel)
          Screen.ACCOUNT -> AccountScreen(
            viewModel = viewModel,
            onOpenAlerts = {},
            onOpenSuggestions = {},
            onOpenAdmin = {}
          )
        }
      }
    }

    // Wait for the seeded stations to flow out of Room
    waitFor("seeded stations") { viewModel.approvedStations.value.isNotEmpty() }

    capture("home_screen")

    screen = Screen.MAP
    capture("map_screen")

    screen = Screen.DETAILS
    capture("place_details")

    screen = Screen.WIZARD
    capture("report_wizard")

    // Register a profile so rewards/account show real user data
    viewModel.authNameInput = "أحمد التجريبي"
    viewModel.authPhoneInput = "07701234567"
    viewModel.authCityInput = viewModel.approvedCities.value.firstOrNull()?.id
      ?: error("cities not seeded")
    viewModel.registerAndLogin()
    waitFor("registered user") { viewModel.currentUser != null }

    screen = Screen.REWARDS
    capture("rewards_screen")

    screen = Screen.ACCOUNT
    capture("account_screen")
  }

  private fun capture(name: String) {
    shadowOf(Looper.getMainLooper()).idle()
    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/$name.png")
  }

  /**
   * Compose's waitUntil does not pump Robolectric's paused main looper, where
   * viewModelScope coroutines (and IO→Main resumptions) are queued — idle it
   * explicitly on every poll so VM work can make progress.
   */
  private fun waitFor(what: String, condition: () -> Boolean) {
    val deadline = System.currentTimeMillis() + 30_000
    while (!condition()) {
      if (System.currentTimeMillis() > deadline) {
        throw AssertionError("Timed out waiting for $what")
      }
      shadowOf(Looper.getMainLooper()).idle()
      composeTestRule.waitForIdle()
      Thread.sleep(50)
    }
    shadowOf(Looper.getMainLooper()).idle()
  }
}

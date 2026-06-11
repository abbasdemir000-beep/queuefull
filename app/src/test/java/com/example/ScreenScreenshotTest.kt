package com.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.QueueFuelDatabase
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
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Full-screen captures of the redesigned surfaces, rendered RTL against the
 * real ViewModel + seeded Room database (Robolectric). CI uploads
 * src/test/screenshots/ as the "ui-screenshots" artifact for visual review.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ScreenScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  // The Room singleton outlives Robolectric's per-test filesystem; reset it so
  // every test seeds a fresh database instead of reusing a stale connection.
  @After
  fun resetDatabaseSingleton() {
    val field = QueueFuelDatabase::class.java.getDeclaredField("INSTANCE")
    field.isAccessible = true
    (field.get(null) as? QueueFuelDatabase)?.close()
    field.set(null, null)
  }

  private fun newViewModel(): QueueFuelViewModel =
    QueueFuelViewModel(ApplicationProvider.getApplicationContext())

  /** The app renders RTL under the Arabic locale; force it for captures. */
  @Composable
  private fun Rtl(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      MyApplicationTheme { content() }
    }
  }

  private fun waitForStations(viewModel: QueueFuelViewModel) {
    composeTestRule.waitUntil(timeoutMillis = 20_000) {
      viewModel.approvedStations.value.isNotEmpty()
    }
  }

  private fun registerTestUser(viewModel: QueueFuelViewModel) {
    composeTestRule.waitUntil(timeoutMillis = 20_000) {
      viewModel.approvedCities.value.isNotEmpty()
    }
    viewModel.authNameInput = "أحمد التجريبي"
    viewModel.authPhoneInput = "07701234567"
    viewModel.authCityInput = viewModel.approvedCities.value.first().id
    viewModel.registerAndLogin()
    composeTestRule.waitUntil(timeoutMillis = 20_000) { viewModel.currentUser != null }
  }

  @Test
  fun home_screen_screenshot() {
    val viewModel = newViewModel()
    composeTestRule.setContent {
      Rtl { HomeScreen(viewModel = viewModel, onOpenAlerts = {}, onStationClick = {}) }
    }
    waitForStations(viewModel)

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home_screen.png")
  }

  @Test
  fun map_screen_screenshot() {
    val viewModel = newViewModel()
    composeTestRule.setContent {
      Rtl { MapScreen(viewModel = viewModel, onOpenDetails = {}) }
    }
    waitForStations(viewModel)

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/map_screen.png")
  }

  @Test
  fun place_details_screenshot() {
    val viewModel = newViewModel()
    composeTestRule.setContent {
      val stations by viewModel.approvedStations.collectAsState()
      Rtl {
        if (stations.isNotEmpty()) {
          PlaceDetailsScreen(
            station = stations.first(),
            viewModel = viewModel,
            onBack = {},
            onNewReport = {}
          )
        }
      }
    }
    waitForStations(viewModel)

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/place_details.png")
  }

  @Test
  fun report_wizard_screenshot() {
    val viewModel = newViewModel()
    composeTestRule.setContent {
      Rtl { ReportWizardScreen(viewModel = viewModel, initialStation = null, onClose = {}) }
    }
    waitForStations(viewModel)

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/report_wizard.png")
  }

  @Test
  fun rewards_screen_screenshot() {
    val viewModel = newViewModel()
    composeTestRule.setContent {
      Rtl { RewardsScreen(viewModel = viewModel) }
    }
    registerTestUser(viewModel)

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/rewards_screen.png")
  }

  @Test
  fun account_screen_screenshot() {
    val viewModel = newViewModel()
    composeTestRule.setContent {
      Rtl {
        AccountScreen(
          viewModel = viewModel,
          onOpenAlerts = {},
          onOpenSuggestions = {},
          onOpenAdmin = {}
        )
      }
    }
    registerTestUser(viewModel)

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/account_screen.png")
  }
}

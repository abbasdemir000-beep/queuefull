package com.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.domain.model.QueueCategory
import com.example.ui.components.QfCategoriesGrid
import com.example.ui.components.QfStatusBadge
import com.example.ui.components.QueueFuelSplashScreen
import com.example.ui.theme.QueueFuelTheme
import com.example.ui.theme.QfClosed
import com.example.ui.theme.QfError
import com.example.ui.theme.QfSuccess
import com.example.ui.theme.QfWarning
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class BrandScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun splash_screen_screenshot() {
    composeTestRule.setContent { QueueFuelTheme { QueueFuelSplashScreen(onFinished = {}) } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/splash_screen.png")
  }

  @Test
  fun categories_grid_screenshot() {
    composeTestRule.setContent {
      QueueFuelTheme {
        QfCategoriesGrid(selectedCategory = QueueCategory.FUEL, onCategoryClick = {})
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/categories_grid.png")
  }

  @Test
  fun status_badges_screenshot() {
    composeTestRule.setContent {
      QueueFuelTheme {
        Row(
          modifier = Modifier.padding(8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          QfStatusBadge("فارغة", QfSuccess)
          QfStatusBadge("متوسطة", QfWarning)
          QfStatusBadge("مزدحمة", QfError)
          QfStatusBadge("مغلقة", QfClosed)
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/status_badges.png")
  }
}

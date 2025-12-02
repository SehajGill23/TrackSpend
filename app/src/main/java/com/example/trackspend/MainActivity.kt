package com.example.trackspend

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.example.trackspend.data.local.DatabaseModule
import com.example.trackspend.navigation.AppNavHost
import com.example.trackspend.navigation.BottomNavBar
import com.example.trackspend.ui.theme.TrackSpendTheme


/**
 * The main entry point for the TrackSpend application.
 *
 * <p>This activity sets up the core app environment, initializes database modules,
 * configures system UI (status bar / edge-to-edge), and loads the Jetpack Compose
 * UI hierarchy.</p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Enables edge-to-edge drawing for a modern full-screen layout.</li>
 *   <li>Sets light/dark status bar appearance based on system theme.</li>
 *   <li>Initializes Room database via {@code DatabaseModule.init()}.</li>
 *   <li>Loads the Compose theme and launches {@link MainScreen}.</li>
 * </ul>
 *
 * @see MainScreen
 */
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)

    /**
     * Called when the activity is first created.
     *
     * <p>Configures system window behavior, initializes application-level
     * dependencies, and sets the Compose content using the app's theme.</p>
     *
     * @param savedInstanceState Saved instance state from system recreation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        DatabaseModule.init(applicationContext)
        setContent {
            TrackSpendTheme {
                MainScreen()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable

/**
 * Root composable for the TrackSpend application.
 *
 * <p>Sets up the navigation controller, bottom navigation bar, and the
 * {@link AppNavHost} that manages all screens in the app.</p>
 *
 * <p><b>Layout Structure:</b></p>
 * <ul>
 *   <li>Scaffold with a persistent bottom navigation bar.</li>
 *   <li>Content area populated by the navigation host.</li>
 * </ul>
 *
 * @see AppNavHost
 * @see BottomNavBar
 */
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
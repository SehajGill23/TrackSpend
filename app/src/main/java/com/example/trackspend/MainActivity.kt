package com.example.trackspend

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.trackspend.navigation.AppNavHost
import com.example.trackspend.navigation.BottomNavBar
import com.example.trackspend.ui.theme.TrackSpendTheme
import  androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsControllerCompat
import com.example.trackspend.data.local.DatabaseModule


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
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
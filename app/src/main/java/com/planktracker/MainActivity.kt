package com.planktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.planktracker.ui.PlankNavigation
import com.planktracker.ui.theme.PlankTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openTimer = intent?.getBooleanExtra("open_timer", false) ?: false

        setContent {
            PlankTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlankNavigation(openTimerOnStart = openTimer)
                }
            }
        }
    }
}

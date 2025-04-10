package com.samuelseptiano.speedreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.samuelseptiano.speedreminder.ui.screen.HomeScreen
import com.samuelseptiano.speedreminder.ui.theme.SpeedReminderTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpeedReminderTheme {
                HomeScreen(this@MainActivity)
            }
        }
    }




}
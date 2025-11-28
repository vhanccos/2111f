package com.example.apphilos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.apphilos.ui.screens.OrderProcessorScreen
import com.example.apphilos.ui.theme.AppHilosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppHilosTheme {
                OrderProcessorScreen()
            }
        }
    }
}
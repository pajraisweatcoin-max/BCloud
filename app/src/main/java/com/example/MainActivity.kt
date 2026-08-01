package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.service.BarraServerService
import com.example.ui.BarraApp
import com.example.ui.theme.BarraCloudTheme
import com.example.ui.viewmodel.BarraViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BarraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start Foreground Server Service
        startBarraServerService()

        setContent {
            BarraCloudTheme {
                BarraApp(viewModel = viewModel)
            }
        }
    }

    private fun startBarraServerService() {
        val intent = Intent(this, BarraServerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

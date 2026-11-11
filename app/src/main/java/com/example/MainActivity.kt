package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.NovaBrowserApp
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {
    private val browserViewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming http/https VIEW intents when Nova Browser is used to open external links
        handleIncomingIntent(intent)

        setContent {
            NovaBrowserApp(viewModel = browserViewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val dataUrl = intent?.dataString
        if (!dataUrl.isNullOrBlank() &&
            (dataUrl.startsWith("http://", ignoreCase = true) ||
                dataUrl.startsWith("https://", ignoreCase = true))
        ) {
            browserViewModel.openNewTab(url = dataUrl, isIncognito = false)
        }
    }
}

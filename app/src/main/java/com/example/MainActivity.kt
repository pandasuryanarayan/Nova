package com.example

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import com.example.ui.NetraBrowserApp
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {
    private val browserViewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val launchRealtimeMs = SystemClock.elapsedRealtime()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
        ) {
            val contentView = findViewById<View>(android.R.id.content)
            contentView.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        val elapsed = SystemClock.elapsedRealtime() - launchRealtimeMs
                        return if (elapsed >= 680L) {
                            contentView.viewTreeObserver.removeOnPreDrawListener(this)
                            true
                        } else {
                            contentView.postInvalidateDelayed(16L)
                            false
                        }
                    }
                }
            )

            splashScreen.setOnExitAnimationListener { splashScreenView ->
                val fadeOut = ObjectAnimator.ofFloat(splashScreenView, View.ALPHA, 1f, 0f).apply {
                    interpolator = DecelerateInterpolator()
                    duration = 180L
                    doOnEnd { splashScreenView.remove() }
                }
                fadeOut.start()
            }
        }

        enableEdgeToEdge()

        // Handle incoming http/https VIEW intents when Netra Browser is used to open external links
        handleIncomingIntent(intent)

        setContent {
            NetraBrowserApp(viewModel = browserViewModel)
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

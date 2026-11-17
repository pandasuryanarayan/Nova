package com.example

import android.animation.ObjectAnimator
import android.app.SearchManager
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
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

        if (savedInstanceState == null) {
            handleIncomingIntent(intent)
        }

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
        val target = extractTargetFromBrowserIntent(intent)
        if (!target.isNullOrBlank()) {
            browserViewModel.openExternalLinkOrSearch(target)
        }
    }

    companion object {
        internal fun extractTargetFromBrowserIntent(intent: Intent?): String? {
            if (intent == null) return null
            return when (intent.action) {
                Intent.ACTION_VIEW,
                NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                    val dataUrl = intent.dataString?.trim()
                    if (!dataUrl.isNullOrBlank() &&
                        (dataUrl.startsWith("http://", ignoreCase = true) ||
                            dataUrl.startsWith("https://", ignoreCase = true) ||
                            dataUrl.startsWith("about:", ignoreCase = true) ||
                            dataUrl.startsWith("netra://", ignoreCase = true) ||
                            dataUrl.startsWith("content://", ignoreCase = true) ||
                            dataUrl.startsWith("file://", ignoreCase = true))
                    ) {
                        dataUrl
                    } else {
                        null
                    }
                }
                Intent.ACTION_WEB_SEARCH,
                Intent.ACTION_SEARCH,
                MediaStore.INTENT_ACTION_MEDIA_SEARCH -> {
                    intent.getStringExtra(SearchManager.QUERY)?.takeIf { it.isNotBlank() }
                        ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }
                        ?: intent.dataString?.takeIf { it.isNotBlank() }
                }
                Intent.ACTION_SEND -> {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }
                }
                else -> {
                    val fallbackUrl = intent.dataString?.trim()
                    if (!fallbackUrl.isNullOrBlank() &&
                        (fallbackUrl.startsWith("http://", ignoreCase = true) ||
                            fallbackUrl.startsWith("https://", ignoreCase = true))
                    ) {
                        fallbackUrl
                    } else {
                        null
                    }
                }
            }
        }
    }
}


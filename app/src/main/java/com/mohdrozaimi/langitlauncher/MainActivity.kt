package com.mohdrozaimi.langitlauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.lifecycle.Lifecycle
import com.mohdrozaimi.langitlauncher.ui.HomeViewModel
import com.mohdrozaimi.langitlauncher.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    private val vm: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        // Launcher: butang Back di skrin utama tidak menutup app.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            },
        )
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                HomeScreen(vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onNewIntent(intent: Intent) {
        // Masih kelihatan (STARTED) = butang Home ditekan semasa di skrin utama.
        val wasVisible = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && wasVisible) vm.onHomePressed()
    }
}

package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.example.data.provider.LrcLibLyricsProvider
import com.example.ui.GammaApp
import com.example.ui.theme.GammaTheme
import com.example.ui.theme.GammaThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GammaThemeManager.init(this)
        LrcLibLyricsProvider.initCache(cacheDir)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            GammaTheme {
                GammaApp()
            }
        }
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            val isCtrl = event.isCtrlPressed || event.isMetaPressed
            if (isCtrl && event.keyCode == android.view.KeyEvent.KEYCODE_K) {
                com.example.core.ui.GammaCommandPaletteManager.toggle()
                return true
            }
            if (event.keyCode == android.view.KeyEvent.KEYCODE_ESCAPE && com.example.core.ui.GammaCommandPaletteManager.isVisible) {
                com.example.core.ui.GammaCommandPaletteManager.close()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}

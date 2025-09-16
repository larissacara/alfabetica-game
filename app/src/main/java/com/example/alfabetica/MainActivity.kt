package com.example.alfabetica

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.example.alfabetica.ui.theme.AlfabeticaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Permite que o conteúdo trate insets (inclui IME) e ajusta para teclado
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContent {
            AlfabeticaTheme { 
                var showGame by remember { mutableStateOf(false) }
                var selectedCategory by remember { mutableStateOf("FILME") }
                
                if (showGame) {
                    GameScreen(
                        category = selectedCategory,
                        onBack = { showGame = false },
                        onLetter = { /* Handle letter selection */ },
                        onPlay = { /* Handle play button */ }
                    )
                } else {
                    MenuScreen(
                        onPlay = { showGame = true }
                    )
                }
            }
        }
    }
}

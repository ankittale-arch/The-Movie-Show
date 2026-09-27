package com.ankitt.themovieshow

import android.os.Bundle
import androidx.activity.*
import androidx.activity.compose.setContent
import com.ankitt.themovieshow.core.designsystem.theme.TheMovieShowTheme
import com.ankitt.themovieshow.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheMovieShowTheme {
                AppNavHost()
            }
        }
    }
}

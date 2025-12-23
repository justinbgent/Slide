package com.edgeline.slider.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edgeline.slider.model.Screen
import com.edgeline.slider.ui.compose.Credits
import com.edgeline.slider.ui.compose.MainMenu
import com.edgeline.slider.ui.compose.Game
import com.edgeline.slider.ui.theme.SliderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SliderTheme {
                Screens()
            }
        }
    }
}

@Composable
fun Screens(){
    val navController = rememberNavController()
    val onNavigateBack: () -> Unit = {
        navController.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Screen.MainMenu.name) {
            composable(Screen.MainMenu.name) {
                MainMenu(
                    startGame = {
                        navController.navigate(Screen.Game.name)
                    },
                    navigateToCredits = {
                        navController.navigate(Screen.Credits.name)
                    }
                )
            }
            composable(Screen.Game.name) {
                Game(onNavigateBack)
            }
            composable(Screen.Credits.name) {
                Credits(onNavigateBack)
            }
        }
    }
}
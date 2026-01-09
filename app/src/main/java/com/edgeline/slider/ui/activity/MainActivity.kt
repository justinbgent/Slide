package com.edgeline.slider.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edgeline.slider.game.model.Window
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

        NavHost(navController = navController, startDestination = Window.MainMenu.name) {
            composable(Window.MainMenu.name) {
                MainMenu(
                    startGame = {
                        navController.navigate(Window.Game.name)
                    },
                    navigateToCredits = {
                        navController.navigate(Window.Credits.name)
                    }
                )
            }
            composable(Window.Game.name) {
                Game(onNavigateBack)
            }
            composable(Window.Credits.name) {
                Credits(onNavigateBack)
            }
        }

}
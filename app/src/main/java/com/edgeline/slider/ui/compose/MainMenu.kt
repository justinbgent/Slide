package com.edgeline.slider.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenu(
    startGame: () -> Unit,
    navigateToCredits: () -> Unit
) {
    Scaffold { insets ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(insets),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = startGame, modifier = Modifier.padding(8.dp)) {
                Text(text = "Start Game")
            }
            Button(onClick = navigateToCredits, modifier = Modifier.padding(8.dp)) {
                Text(text = "Credits")
            }
        }
    }
}
package com.edgeline.slider.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Credits(
    onNavigateBack: () -> Unit
) {
    Scaffold { insets ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(insets),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.End
        ) {
            Button(onClick = onNavigateBack) {
                Text(text = "Return to Menu")
            }
        }
    }
}
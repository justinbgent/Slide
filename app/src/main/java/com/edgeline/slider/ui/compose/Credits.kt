package com.edgeline.slider.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun Credits(
    onNavigateBack: () -> Unit
) {
    Scaffold { insets ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = insets.calculateTopPadding(),
                    bottom = insets.calculateBottomPadding(),
                    start = 16.dp + insets.calculateStartPadding(LayoutDirection.Ltr),
                    end = 16.dp + insets.calculateEndPadding(LayoutDirection.Ltr)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.End
        ) {
            Button(onClick = onNavigateBack) {
                Text(text = "Return to Menu")
            }
        }
    }
}
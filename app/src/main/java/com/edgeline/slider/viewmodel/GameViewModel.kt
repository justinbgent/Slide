package com.edgeline.slider.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class GameViewModel : ViewModel() {
    val logicScope = CoroutineScope(Dispatchers.Default)
}
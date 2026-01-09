package com.edgeline.slider.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.edgeline.slider.application.SliderApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            GameViewModel(
                sliderApplication().database.scoreDao()
            )
        }
    }
}

fun CreationExtras.sliderApplication(): SliderApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SliderApplication)

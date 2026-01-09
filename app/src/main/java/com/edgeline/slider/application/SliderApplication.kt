package com.edgeline.slider.application

import android.app.Application
import com.edgeline.slider.room.AppDatabase

class SliderApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.Companion.getDatabase(this) }
}
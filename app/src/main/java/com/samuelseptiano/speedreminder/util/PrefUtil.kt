package com.samuelseptiano.speedreminder.util

import android.app.Service.MODE_PRIVATE
import android.content.Context

/**
 * Created by samuel.septiano on 08/04/2025.
 */

val PREFS_NAME = "location_prefs"
val PREFS_SPEED = "prefs_speed"
val PREFS_LAT = "prefs_lat"
val PREFS_LON = "prefs_lon"

fun Context.savePrefsData(speed: Float, lat: Float, lon: Float) {
    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        .edit()
        .putFloat(PREFS_SPEED, speed)
        .putFloat(PREFS_LAT, lat)
        .putFloat(PREFS_LON, lon)
        .apply()
}

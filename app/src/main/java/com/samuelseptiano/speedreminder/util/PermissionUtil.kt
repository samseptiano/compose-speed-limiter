package com.samuelseptiano.speedreminder.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Created by samuel.septiano on 08/04/2025.
 */

fun getListPermission(): List<String> {
    return listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
}

fun Context.requestMultiplePermissions(): MutableList<String> {
    val permissionsToRequest = mutableListOf<String>()

    getListPermission().forEach {
        if (ContextCompat.checkSelfPermission(this, it)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(it)
        }
    }

    return permissionsToRequest
}


fun Context.checkMultiplePermission(): Boolean {
    getListPermission().forEach {
        if (ContextCompat.checkSelfPermission(this, it)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }
    return true
}

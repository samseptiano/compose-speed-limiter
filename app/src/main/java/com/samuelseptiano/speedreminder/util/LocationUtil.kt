package com.samuelseptiano.speedreminder.util

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.io.IOException
import java.util.Locale

/**
 * Created by samuel.septiano on 08/04/2025.
 */
object LocationUtil {

    val MIN_INTERVAL = 1000L
    val MIN_DISTANCE = 1f

    fun calculateSpeed(speedInMetersPerSecond:Float):Double{
        return speedInMetersPerSecond * 3.6
    }

    fun calculateSpeedString(speedInMetersPerSecond:Float):String{
        return "Speed: ${calculateSpeed(speedInMetersPerSecond)} km/h"
    }

    fun Context.showPermissionSettingsDialog(permission: String) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Please enable $permission in settings to continue using this feature.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    @Suppress("DEPRECATION")
    fun getAddressFromLatLng(ctx: Context, lat: Double, lon: Double): String {
        val geocoder = Geocoder(ctx, Locale.getDefault())
        var addressText = "Address not found"

        try {
            // Get the address based on lat/lon
            val addresses = geocoder.getFromLocation(lat, lon, 5)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                // Build address string (you can format it as needed)
                val addressBuilder = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    addressBuilder.append(address.getAddressLine(i)).append(" ")
                }

                addressText = addressBuilder.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace() // Handle exception
            Log.d("error parsing address", e.toString())
        }

        return addressText
    }

    @Suppress("DEPRECATION")
    fun isMockLocation(location:Location):Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            location.isFromMockProvider
        }
    }


}
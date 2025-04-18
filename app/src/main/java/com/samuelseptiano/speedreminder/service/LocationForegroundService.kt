package com.samuelseptiano.speedreminder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.samuelseptiano.speedreminder.MainActivity
import com.samuelseptiano.speedreminder.R
import com.samuelseptiano.speedreminder.util.Constant.INTENT_KEY_IS_FAKE_GPS
import com.samuelseptiano.speedreminder.util.Constant.INTENT_KEY_LAT
import com.samuelseptiano.speedreminder.util.Constant.INTENT_KEY_LON
import com.samuelseptiano.speedreminder.util.Constant.INTENT_KEY_SPEED
import com.samuelseptiano.speedreminder.util.LocationUtil.MIN_INTERVAL
import com.samuelseptiano.speedreminder.util.LocationUtil.calculateSpeed
import com.samuelseptiano.speedreminder.util.checkMultiplePermission
import com.samuelseptiano.speedreminder.util.convertTo2PlacesDecimal
import com.samuelseptiano.speedreminder.util.setAlertSound

/**
 * Created by samuel.septiano on 08/04/2025.
 */
class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latState: Float = 0f
    private var lonState: Float = 0f
    private var speedState: Float = 0f
    private val channelId = "timer_channel"


    val locationCallback: LocationCallback =
        object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    latState = location.latitude.toFloat()
                    lonState = location.longitude.toFloat()

                    val speedInMetersPerSecond = location.speed
                    val speedInKmPerHour = calculateSpeed(speedInMetersPerSecond)
                    speedState = convertTo2PlacesDecimal(speedInKmPerHour).toFloat()
                    Log.d("speedState service", speedState.toString())
                    setAlertSound(speedState)

                    val intent = Intent("com.example.ACTION_SEND_DATA")
                    intent.putExtra(INTENT_KEY_SPEED, speedState)
                    intent.putExtra(INTENT_KEY_LON, lonState)
                    intent.putExtra(INTENT_KEY_LAT, latState)
                    intent.putExtra(INTENT_KEY_IS_FAKE_GPS, location.isMock)

                    sendBroadcast(intent)

                    updateNotification(speedState, latState, lonState)

                }
            }
        }


    override fun onCreate() {
        super.onCreate()
        Log.d("ForegroundService", "Service Created")
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@LocationForegroundService)
        createNotificationChannel() // Ensure it's created before startForeground

        if (this@LocationForegroundService.checkMultiplePermission()) {
            locationCallback.let {

                // Create and configure the LocationRequest
                val locationRequest = LocationRequest.create()
                locationRequest.setInterval(MIN_INTERVAL) // Update every 10 seconds
                locationRequest.setFastestInterval(MIN_INTERVAL) // Fastest update every 5 seconds
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    it,
                    Looper.getMainLooper()
                )
            }
        }


        val initialNotification = createNotification(0f, 0f, 0f)
        startForeground(1, initialNotification) // Must be called immediately after starting
    }


    private fun createNotification(speed: Float, lat: Float, lon: Float): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Running")
            .setContentText("Speed: $speed km/h Lat: $lat Lon: $lon")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // prevent swipe-dismiss
            .build()
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Timer Service",
                    NotificationManager.IMPORTANCE_LOW
                )
                channel.description = "Shows timer running in background"
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun updateNotification(speed: Float, lat: Float, lon: Float) {
        val notification = createNotification(speed, lat, lon)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ForegroundService", "Service Started")
        return START_STICKY // Ensures service restarts if killed
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

package com.samuelseptiano.speedreminder.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.samuelseptiano.speedreminder.service.LocationForegroundService
import com.samuelseptiano.speedreminder.util.DateUtil.getCurrentTimeStamp
import com.samuelseptiano.speedreminder.util.LocationUtil.MIN_INTERVAL
import com.samuelseptiano.speedreminder.util.LocationUtil.calculateSpeed
import com.samuelseptiano.speedreminder.util.LocationUtil.getAddressFromLatLng
import com.samuelseptiano.speedreminder.util.LocationUtil.isMockLocation
import com.samuelseptiano.speedreminder.util.LocationUtil.showPermissionSettingsDialog
import com.samuelseptiano.speedreminder.util.PREFS_LAT
import com.samuelseptiano.speedreminder.util.PREFS_LON
import com.samuelseptiano.speedreminder.util.PREFS_NAME
import com.samuelseptiano.speedreminder.util.PREFS_SPEED
import com.samuelseptiano.speedreminder.util.checkMultiplePermission
import com.samuelseptiano.speedreminder.util.convertTo2PlacesDecimal
import com.samuelseptiano.speedreminder.util.getListPermission
import com.samuelseptiano.speedreminder.util.requestMultiplePermissions
import com.samuelseptiano.speedreminder.util.savePrefsData
import kotlinx.coroutines.delay

/**
 * Created by samuel.septiano on 08/04/2025.
 */
@Composable
fun HomeScreen(activity: Activity) {
    val context = LocalContext.current

    var isFakeGPS by remember { mutableStateOf(false) }

    var speedState by remember { mutableDoubleStateOf(0.0) }
    var latState by remember { mutableDoubleStateOf(0.0) }
    var lonState by remember { mutableDoubleStateOf(0.0) }

    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    val locationCallback: LocationCallback = remember {
        object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    latState = location.latitude
                    lonState = location.longitude
                    isFakeGPS = isMockLocation(location)

                    val speedInMetersPerSecond = location.speed
                    val speedInKmPerHour = calculateSpeed(speedInMetersPerSecond)
                    speedState = convertTo2PlacesDecimal(speedInKmPerHour)

                    context.savePrefsData(
                        speedState.toFloat(),
                        latState.toFloat(),
                        lonState.toFloat()
                    )
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = mutableListOf<String>()

        getListPermission().forEach { permission ->
            val granted = permissions[permission] == true
            if (!granted) {
                // Permission denied
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    // User denied permission but didn't check "Don't ask again"
                    deniedPermissions.add(permission)
                } else {
                    // User denied with "Don't ask again" or permanently denied
                    context.showPermissionSettingsDialog(permission)
                    return@forEach
                }
            }
        }

        if (deniedPermissions.isNotEmpty()) {
            // Try again

        } else {
            // All permissions granted, proceed
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
    }


    fun stopLocationForgroundService() {
        activity.stopService(Intent(activity, LocationForegroundService::class.java))
    }

    fun startLocationForegroundService(context: Context) {
        val intent = Intent(context, LocationForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun registerLocationUpdate() {
        val permissionsToRequest = context.requestMultiplePermissions()
        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest.toTypedArray())
        }
        if (context.checkMultiplePermission()) {
            locationCallback.let {

                // Create and configure the LocationRequest
                val locationRequest = com.google.android.gms.location.LocationRequest.create()
                locationRequest.setInterval(MIN_INTERVAL) // Update every 10 seconds
                locationRequest.setFastestInterval(MIN_INTERVAL) // Fastest update every 5 seconds
                locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    it,
                    Looper.getMainLooper()
                )
            }
        }
    }


    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val speed = prefs.getFloat(PREFS_SPEED, 0f)
        val lat = prefs.getFloat(PREFS_LAT, 0f)
        val lon = prefs.getFloat(PREFS_LON, 0f)

        if (lat != 0f && lon != 0f) {
            speedState = speed.toDouble()
            latState = lat.toDouble()
            lonState = lon.toDouble()
        }

        registerLocationUpdate()
        startLocationForegroundService(context)

    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Greeting(name = "Android")
                ShowCoordinate(lat = latState, lon = lonState)
                ShowAddress(ctx = context, lat = latState, lon = lonState)
                ShowSpeed(speed = speedState)
                ShowTimeStamp()
                if (isFakeGPS) {
                    Text(
                        text = "Fake GPS",
                        color = Color.Red
                    )
                }
            }
        }
    }


}

@Composable
fun ShowCoordinate(lat: Double, lon: Double, modifier: Modifier = Modifier) {

    Text(
        text = "Coordinate: $lat, $lon",
        modifier = modifier
    )
}

@Composable
fun ShowSpeed(speed: Double, modifier: Modifier = Modifier) {
    Text(
        text = "Speed: ${speed} km/h",
        modifier = modifier
    )
}

@Composable
fun ShowAddress(ctx: Context, lat: Double, lon: Double, modifier: Modifier = Modifier) {
    Text(
        text = "Address: ${getAddressFromLatLng(ctx, lat, lon)}",
        modifier = modifier
    )
}


@Composable
fun ShowTimeStamp(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(getCurrentTimeStamp()) }

    LaunchedEffect(Unit) {
        while (true) {
            time = getCurrentTimeStamp()
            delay(1000) // update every second
        }
    }

    Text(
        text = time,
        modifier = modifier
    )
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


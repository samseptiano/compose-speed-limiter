package com.samuelseptiano.speedreminder.ui.screen

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.samuelseptiano.speedreminder.R
import com.samuelseptiano.speedreminder.service.LocationForegroundService
import com.samuelseptiano.speedreminder.util.Constant.INTENT_KEY_IS_FAKE_GPS
import com.samuelseptiano.speedreminder.util.Constant.INTENT_KEY_LAT
import com.samuelseptiano.speedreminder.util.Constant.INTENT_KEY_LON
import com.samuelseptiano.speedreminder.util.Constant.INTENT_KEY_SPEED
import com.samuelseptiano.speedreminder.util.DateUtil.getCurrentTimeStamp
import com.samuelseptiano.speedreminder.util.LocationUtil.getAddressFromLatLng
import com.samuelseptiano.speedreminder.util.LocationUtil.showPermissionSettingsDialog
import com.samuelseptiano.speedreminder.util.checkMultiplePermission
import com.samuelseptiano.speedreminder.util.convertTo2PlacesDecimal
import com.samuelseptiano.speedreminder.util.getListPermission
import com.samuelseptiano.speedreminder.util.requestMultiplePermissions
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Created by samuel.septiano on 08/04/2025.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomeScreen(activity: Activity) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val receiverRef = remember { mutableStateOf<BroadcastReceiver?>(null) }

    var isFakeGPS by remember { mutableStateOf(false) }

    var speedState by remember { mutableDoubleStateOf(0.0) }
    var latState by remember { mutableDoubleStateOf(0.0) }
    var lonState by remember { mutableDoubleStateOf(0.0) }
    var isPermissionGrantedState by remember { mutableStateOf(false) }


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
            isPermissionGrantedState = true
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
            isPermissionGrantedState = true
        }
    }

    // Receiver setup
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                speedState = convertTo2PlacesDecimal(intent?.getFloatExtra(INTENT_KEY_SPEED, 0f)?.toDouble() ?: 0.0)
                latState = intent?.getFloatExtra(INTENT_KEY_LAT, 0f)?.toDouble() ?: 0.0
                lonState = intent?.getFloatExtra(INTENT_KEY_LON, 0f)?.toDouble() ?: 0.0
                isFakeGPS = intent?.getBooleanExtra(INTENT_KEY_IS_FAKE_GPS, false)?:false

                Log.d("speedState", speedState.toString())
            }
        }

        val filter = IntentFilter("com.example.ACTION_SEND_DATA")
        appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        receiverRef.value = receiver


        onDispose {
            receiverRef.value?.let {
                try {
                    appContext.unregisterReceiver(it)
                } catch (e: Exception) {
                    Log.w("BroadcastReceiver", "Unregister failed: ${e.message}")
                }
                receiverRef.value = null
            }
        }
    }


    LaunchedEffect(isPermissionGrantedState) {

        registerLocationUpdate()

        if (isPermissionGrantedState) {
            startLocationForegroundService(context)
        }

    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Map takes the full screen
            MapScreen(
                latState = latState,
                lonState = lonState,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay UI
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(0.dp)
                    .background(Color.White.copy(alpha = 1f), shape = RoundedCornerShape(0.dp))
                    .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                ShowTimeStamp()
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // Speed Box
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row {
                        ShowSpeed(speed = speedState)
                        Spacer(modifier = Modifier.width(8.dp)) // Gap between sections
                        if (isFakeGPS) {
                            Text(
                                text = "Fake GPS",
                                color = Color.Red
                            )
                        }
                    }

                }

                Spacer(modifier = Modifier.height(8.dp)) // Gap between sections

                // Coordinate + Address Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    ShowCoordinate(lat = latState, lon = lonState)
                    ShowAddress(ctx = context, lat = latState, lon = lonState)
                }
            }

        }
    }


}









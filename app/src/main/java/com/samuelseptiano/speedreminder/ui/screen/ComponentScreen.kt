package com.samuelseptiano.speedreminder.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.samuelseptiano.speedreminder.util.Constant.MAX_SPEED
import com.samuelseptiano.speedreminder.util.DateUtil.getCurrentTimeStamp
import com.samuelseptiano.speedreminder.util.LocationUtil.getAddressFromLatLng
import kotlinx.coroutines.delay

/**
 * Created by samuel.septiano on 18/04/2025.
 */

@Composable
fun ShowCoordinate(lat: Double, lon: Double, modifier: Modifier = Modifier) {

    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$lat, $lon",
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun ShowSpeed(speed: Double, modifier: Modifier = Modifier) {
    if (speed > MAX_SPEED) {
        Text(
            text = "Your Speed: ${speed} km/h",
            color = Color.Red,
            fontSize = 20.sp,
            modifier = modifier
        )
    } else {
        Text(
            text = "Speed: ${speed} km/h",
            color = Color.White,
            fontSize = 20.sp,
            modifier = modifier
        )
    }
}

@Composable
fun ShowAddress(ctx: Context, lat: Double, lon: Double, modifier: Modifier = Modifier) {
    Text(
        text = "${getAddressFromLatLng(ctx, lat, lon)}",
        color = Color.White,
        fontSize = 14.sp,
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
        fontSize = 11.sp,
        color = Color.Black,
        modifier = modifier
    )
}
package com.samuelseptiano.speedreminder.ui.screen

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by samuel.septiano on 18/04/2025.
 */
@Composable
fun NotificationMapLayout(speed: Float, lat: Float, lon: Float, mapBitmap: Bitmap?) {
    Column(
        modifier = Modifier
            .background(Color.DarkGray)
            .padding(8.dp)
            .width(300.dp)
            .wrapContentHeight()
    ) {
        mapBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Map",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Speed: %.1f km/h".format(speed),
            color = Color.White,
            fontSize = 14.sp
        )

        Text(
            text = "Lat: $lat\nLon: $lon",
            color = Color.White,
            fontSize = 12.sp
        )
    }
}


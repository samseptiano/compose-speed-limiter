package com.samuelseptiano.speedreminder.ui.screen

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.samuelseptiano.speedreminder.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Created by samuel.septiano on 11/04/2025.
 */
@Composable
fun MapScreen(latState: Double, lonState: Double, modifier: Modifier) {
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    BoxWithConstraints(modifier = modifier) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth

        Box(modifier = modifier) {
            // Use the full available screen size as hardcoded dp
            if (latState != 0.0 && lonState != 0.0) {
                OsmMapView(
                    lat = latState,
                    lon = lonState,
                    mapViewRef = mapViewRef,
                    modifier = Modifier
                        .width(screenWidth)
                        .height(screenHeight)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .offset(y = 30.dp)
                    .height(50.dp)
                    .width(50.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .clickable {
                        mapViewRef.value?.let { mapView ->
                            mapView.controller.setCenter(GeoPoint(latState, lonState))
                            mapView.overlays.filterIsInstance<Marker>().firstOrNull()?.apply {
                                position = GeoPoint(latState, lonState)
                            }
                            mapView.invalidate()
                        }
                    }
                    .padding(8.dp) // inner padding around the image
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_maps_center),
                    contentDescription = "Set Center",
                    modifier = Modifier.size(40.dp)
                )
            }

        }
    }


}


@Composable
fun OsmMapView(
    lat: Double,
    lon: Double,
    mapViewRef: MutableState<MapView?>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialPoint = remember { GeoPoint(lat, lon) }

    // Load OSM config once
    DisposableEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", 0))
        onDispose { }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                setBuiltInZoomControls(false)
                controller.setZoom(19.0)
                controller.setCenter(initialPoint)

                val marker = Marker(this).apply {
                    position = initialPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Your Location"
                    icon = ContextCompat.getDrawable(ctx, R.drawable.ic_pinpoint_red) // your drawable here
                }
                overlays.add(marker)

                mapViewRef.value = this
            }
        }
    )
}

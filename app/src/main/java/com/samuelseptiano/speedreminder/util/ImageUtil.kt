package com.samuelseptiano.speedreminder.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by samuel.septiano on 18/04/2025.
 */

suspend fun composeToBitmap(
    context: Context,
    content: @Composable () -> Unit
): Bitmap = withContext(Dispatchers.Main) {
    val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val owner = ComposeView(context).apply {
        setContent {
            content()
        }
    }
    owner.draw(canvas)

    bitmap
}

package com.samuelseptiano.speedreminder.util

import android.content.Context
import android.media.MediaPlayer
import com.samuelseptiano.speedreminder.R

/**
 * Created by samuel.septiano on 08/04/2025.
 */

    fun Context.checkSound(){
        val mediaPlayer = MediaPlayer.create(this, R.raw.cat_meow)
        mediaPlayer.isLooping = true // 🔁 Enable looping
        mediaPlayer.start()
    }

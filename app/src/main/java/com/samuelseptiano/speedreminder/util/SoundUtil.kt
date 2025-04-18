package com.samuelseptiano.speedreminder.util

import android.content.Context
import android.media.MediaPlayer
import com.samuelseptiano.speedreminder.R
import com.samuelseptiano.speedreminder.util.Constant.MAX_SPEED

/**
 * Created by samuel.septiano on 08/04/2025.
 */

    private fun Context.playSound(){
        val mediaPlayer = MediaPlayer.create(this, R.raw.cat_meow)
        mediaPlayer.isLooping = false
        mediaPlayer.start()
    }

    fun Context.setAlertSound(speed:Float){
        when {
            speed > MAX_SPEED -> {
                playSound()
            }
        }

    }

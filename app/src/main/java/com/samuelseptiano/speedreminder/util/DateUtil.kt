package com.samuelseptiano.speedreminder.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by samuel.septiano on 08/04/2025.
 */

object DateUtil {
    private val TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    fun getCurrentTimeStamp(): String {
        val formatter = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        val formatted = formatter.format(Date())
        return formatted
    }
}
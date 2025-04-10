package com.samuelseptiano.speedreminder.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Created by samuel.septiano on 08/04/2025.
 */

fun convertTo2PlacesDecimal(value: Double): Double{
    return BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
}
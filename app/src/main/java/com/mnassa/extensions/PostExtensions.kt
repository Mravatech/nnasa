package com.mnassa.extensions

import com.mnassa.domain.model.LocationPlaceModel

/**
 * Created by Peter on 3/19/2018.
 */
fun Double.formatAsMoney(): String {
    val formatted = (this * 100).toLong() / 100L
    return formatted.toString() + " SAR"
}

fun LocationPlaceModel?.formatted(): String {
    if (this == null) return ""

    val result = StringBuilder()
    result.append(placeName ?: "")
    if (placeName != null && city != null) {
        result.append(", ")
    }
    result.append(city ?: "")
    return result.toString()
}
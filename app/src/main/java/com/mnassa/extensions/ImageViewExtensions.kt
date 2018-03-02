package com.mnassa.extensions

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView


/**
 * Created by Peter on 3/2/2018.
 */
fun ImageView.disable() {
    val matrix = ColorMatrix()
    matrix.setSaturation(0f)  //0 means grayscale
    val cf = ColorMatrixColorFilter(matrix)
    colorFilter = cf
    imageAlpha = 128   // 128 = 0.5
}

fun ImageView.enable() {
    colorFilter = null
    imageAlpha = 255
}
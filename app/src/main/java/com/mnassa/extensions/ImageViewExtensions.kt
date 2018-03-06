package com.mnassa.extensions

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView


/**
 * Created by Peter on 3/2/2018.
 */
private const val GRAYSCALE_SATURATION = 0f
private const val FULL_IMAGE_ALPHA = 255
private const val HALF_IMAGE_ALPHA = FULL_IMAGE_ALPHA / 2

fun ImageView.disable() {
    val matrix = ColorMatrix()
    matrix.setSaturation(GRAYSCALE_SATURATION)
    val cf = ColorMatrixColorFilter(matrix)
    colorFilter = cf
    imageAlpha = HALF_IMAGE_ALPHA
}

fun ImageView.enable() {
    colorFilter = null
    imageAlpha = FULL_IMAGE_ALPHA
}
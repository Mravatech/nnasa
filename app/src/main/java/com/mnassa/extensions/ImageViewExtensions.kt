package com.mnassa.extensions

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mnassa.R


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

fun ImageView.avatar(avatarUrl: String?) {
    //todo: add placeholder, error

    val requestOptions = RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher)

    Glide.with(this)
            .load(avatarUrl)
            .apply(requestOptions)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}
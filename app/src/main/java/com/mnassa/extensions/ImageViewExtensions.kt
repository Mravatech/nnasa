package com.mnassa.extensions

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.helper.GlideApp

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

fun ImageView.avatarRound(avatarUrl: String?) {
    //todo: add placeholder, error

    val storage = context.getInstance<FirebaseStorage>()
    val ref = avatarUrl?.takeIf { it.startsWith("gs://") }?.let { storage.getReferenceFromUrl(it) }

    val requestOptions = RequestOptions().placeholder(R.drawable.empty_ava).error(R.drawable.empty_ava).apply(RequestOptions.circleCropTransform())

    GlideApp.with(this)
            .load(ref ?: avatarUrl ?: "")
            .apply(requestOptions)
            .apply(RequestOptions.circleCropTransform())
            .into(this)
}

fun ImageView.avatarSquare(avatarUrl: String?) {
    //todo: add placeholder, error

    val storage = context.getInstance<FirebaseStorage>()
    val ref = avatarUrl?.takeIf { it.startsWith("gs://") }?.let { storage.getReferenceFromUrl(it) }

    val requestOptions = RequestOptions().placeholder(R.drawable.ic_empty_avatar_placeholder).error(R.drawable.ic_empty_avatar_placeholder)

    GlideApp.with(this)
            .load(ref ?: avatarUrl ?: "")
            .apply(requestOptions)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}

fun ImageView.avatarSquare(avatarUrl: Uri?) {
    //todo: add placeholder, error
    val requestOptions = RequestOptions().placeholder(R.drawable.ic_empty_avatar_placeholder).error(R.drawable.ic_empty_avatar_placeholder)

    GlideApp.with(this)
            .load(avatarUrl ?: "")
            .apply(requestOptions)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}

fun ImageView.image(url: String?, crop: Boolean = true) {
    val storage = context.getInstance<FirebaseStorage>()
    val ref = url?.takeIf { it.startsWith("gs://") }?.let { storage.getReferenceFromUrl(it) }

    val requestOptions = RequestOptions().placeholder(R.drawable.btn_main).error(R.drawable.btn_main)

    var builder = GlideApp.with(this)
            .load(ref ?: url ?: "")
            .apply(requestOptions)

    if (crop) {
        builder = builder.apply(RequestOptions.centerCropTransform())
    }

    builder.into(this)
}

fun ImageView.image(uri: Uri) {
    val requestOptions = RequestOptions().placeholder(R.drawable.ic_empty_avatar_placeholder).error(R.drawable.ic_empty_avatar_placeholder)

    GlideApp.with(this)
            .load(uri)
            .apply(requestOptions)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}
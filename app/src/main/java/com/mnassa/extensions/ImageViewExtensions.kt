package com.mnassa.extensions

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import androidx.core.content.ContextCompat
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.helper.GlideApp
import timber.log.Timber

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
    val placeholder = ContextCompat.getDrawable(context, R.drawable.empty_ava)
    val requestOptions = RequestOptions().placeholder(placeholder).error(placeholder).apply(RequestOptions.circleCropTransform())

    GlideApp.with(this)
            .load(context.processFirebaseUrl(avatarUrl))
            .apply(requestOptions)
            .apply(RequestOptions.circleCropTransform())
            .into(this)
}

fun ImageView.avatarSquare(avatarUrl: String?) {
    val placeholder = ContextCompat.getDrawable(context, R.drawable.ic_empty_ava)
    val requestOptions = RequestOptions().placeholder(placeholder).error(placeholder)

    GlideApp.with(this)
            .load(context.processFirebaseUrl(avatarUrl))
            .apply(requestOptions)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}

fun ImageView.avatarSquare(avatarUrl: Uri?) {
    val placeholder = ContextCompat.getDrawable(context, R.drawable.ic_empty_ava)
    val requestOptions = RequestOptions().placeholder(placeholder).error(placeholder)

    GlideApp.with(this)
            .load(avatarUrl ?: "")
            .apply(requestOptions)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}

fun ImageView.image(url: String?, crop: Boolean = true) {
    val requestOptions = RequestOptions().placeholder(R.drawable.btn_main).error(R.drawable.btn_main)

    var builder = GlideApp.with(this)
            .load(context.processFirebaseUrl(url))
            .apply(requestOptions)

    if (crop) {
        builder = builder.apply(RequestOptions.centerCropTransform())
    }

    builder.into(this)
}

fun ImageView.image(uri: Uri) {
    val placeholder = ContextCompat.getDrawable(context, R.drawable.ic_empty_avatar_placeholder)
    val requestOptions = RequestOptions().placeholder(placeholder).error(placeholder)

    GlideApp.with(this)
            .load(uri)
            .apply(requestOptions)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}

private fun Context.processFirebaseUrl(url: String?): Any {
    try {
        val storage = getInstance<FirebaseStorage>()
        return url?.takeIf { it.startsWith("gs://") }?.let { storage.getReferenceFromUrl(it) }
                ?: url ?: ""
    } catch (e: Exception) {
        Timber.e("Invalid storage bucket: url: $url; project bucket: ${FirebaseApp.getInstance()?.options?.storageBucket}")
    }
    return ""
}
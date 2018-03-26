package com.mnassa.extensions

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.google.firebase.storage.FirebaseStorage
import com.mnassa.R
import com.mnassa.module.GlideApp


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

    val storage = context.appKodein().instance<FirebaseStorage>()
    val ref = avatarUrl?.takeIf { it.startsWith("gs://") }?.let { storage.getReferenceFromUrl(it) }

    val requestOptions = RequestOptions().placeholder(R.drawable.btn_main).error(R.drawable.btn_main).apply(RequestOptions.circleCropTransform())

    GlideApp.with(this)
            .load(ref ?: "")
            .apply(requestOptions)
            .apply(RequestOptions.circleCropTransform())
            .into(this)
}

fun ImageView.avatarSquare(avatarUrl: String?) {
    //todo: add placeholder, error

    val storage = context.appKodein().instance<FirebaseStorage>()
    val ref = avatarUrl?.takeIf { it.startsWith("gs://") }?.let { storage.getReferenceFromUrl(it) }

    val requestOptions = RequestOptions().placeholder(R.drawable.btn_main).error(R.drawable.btn_main)

    GlideApp.with(this)
            .load(ref ?: "")
            .apply(requestOptions)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}

fun ImageView.image(url: String?, crop: Boolean = true) {
    val storage = context.appKodein().instance<FirebaseStorage>()
    val ref = url?.takeIf { it.startsWith("gs://") }?.let { storage.getReferenceFromUrl(it) }

    val requestOptions = RequestOptions().placeholder(R.drawable.btn_main).error(R.drawable.btn_main)

    var builder = GlideApp.with(this)
            .load(ref ?: "")
            .apply(requestOptions)

    if (crop) {
        builder = builder.apply(RequestOptions.centerCropTransform())
    }

    builder.into(this)
}

fun ImageView.image(uri: Uri) {
    val requestOptions = RequestOptions().placeholder(R.drawable.btn_main).error(R.drawable.btn_main)

    GlideApp.with(this)
            .load(uri)
            .apply(requestOptions)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}
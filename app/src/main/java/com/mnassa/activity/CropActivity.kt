package com.mnassa.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.mnassa.R
import com.yalantis.ucrop.UCrop
import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class CropActivity : BaseActivity() {

    private var uri: Uri? = null
    private lateinit var cachedUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filename = "${System.currentTimeMillis()}${getString(R.string.jpg_dimension)}"
        cachedUri = Uri.fromFile(File("$cacheDir${File.separator}$filename"))
        requestPhoto(intent.getIntExtra(PHOTO_INTENT_FLAG, DEFAULT_VALUE))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    ImageSource.CAMERA.ordinal -> cropImage(data?.data ?: uri)
                    ImageSource.GALLERY.ordinal -> cropImage(data?.data)
                    UCrop.REQUEST_CROP -> {
                        setResult(RESULT_OK, intent.putExtra(URI_PHOTO_RESULT, cachedUri))
                        finish()
                    }
                }
            }
            UCrop.RESULT_ERROR, RESULT_CANCELED -> {
                resultWithError()
            }
        }
    }

    private fun requestPhoto(flag: Int) {
        when (flag) {
            ImageSource.GALLERY.ordinal -> requestPhotoGallery()
            ImageSource.CAMERA.ordinal -> requestCamera()
            else -> {
                resultWithError()
            }
        }
    }

    private fun cropImage(uri: Uri?) {
        uri?.let {
            val options = UCrop.Options()
            options.setToolbarColor(ContextCompat.getColor(this, R.color.accent))
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.accent))
            options.setLogoColor(ContextCompat.getColor(this, R.color.white))
            options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.white))
            options.setCompressionQuality(COMPRESSION_QUALITY)
            if (intent.getBooleanExtra(EXTRA_CROP_SQUARE, false)) {
                options.withAspectRatio(1.0f, 1.0f)
            }
            UCrop.of(it, cachedUri)
                    .withOptions(options)
                    .start(this)
        } ?: run {
            resultWithError()
        }
    }

    private fun requestCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (intent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, ImageSource.CAMERA.ordinal)
        } else {
            resultWithError()
        }
    }

    private fun requestPhotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, ImageSource.GALLERY.ordinal)
        } else {
            resultWithError()
        }
    }

    private fun resultWithError() {
        setResult(GET_PHOTO_ERROR)
        finish()
    }

    companion object {
        const val URI_PHOTO_RESULT = "URI_PHOTO_RESULT"
        const val GET_PHOTO_ERROR = 102
        private const val EXTRA_CROP_SQUARE = "EXTRA_CROP_SQUARE"
        private const val PHOTO_INTENT_FLAG = "PHOTO_INTENT_FLAG"
        private const val DEFAULT_VALUE = 0
        private const val COMPRESSION_QUALITY = 40 //Default = 90

        fun start(source: ImageSource, context: Context, cropSquare: Boolean = false): Intent {
            val intent = Intent(context, CropActivity::class.java)
            intent.putExtra(PHOTO_INTENT_FLAG, source.ordinal)
            intent.putExtra(EXTRA_CROP_SQUARE, cropSquare)
            return intent
        }
    }

    enum class ImageSource {
        GALLERY, CAMERA
    }
}
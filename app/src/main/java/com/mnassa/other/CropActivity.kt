package com.mnassa.other

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.yalantis.ucrop.UCrop
import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class CropActivity : AppCompatActivity() {

    private var mUri: Uri? = null
    private var cachedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filename = System.currentTimeMillis().toString() + ".jpg"
        cachedUri = Uri.fromFile(File(cacheDir.toString() + "/" + filename))

        getPhoto(intent.getIntExtra(PHOTO_INTENT_FLAG, 0))

    }


    private fun getPhoto(flag: Int) {
        when (flag) {
            REQUEST_CODE_GALLERY -> requestPhotoGallery()
            REQUEST_CODE_CAMERA -> requestCamera()
            else -> {
                setResult(GET_PHOTO_ERROR)
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CAMERA) {
            mUri = if (data != null) data.data else mUri
            cropImage()
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_GALLERY) {
            mUri = data?.data
            cropImage()
        } else if (resultCode == RESULT_CANCELED) {
            setResult(GET_PHOTO_ERROR)
            finish()
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            setResult(RESULT_OK, intent.putExtra(URI_PHOTO_RESULT, cachedUri))
            finish()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            setResult(GET_PHOTO_ERROR)
            finish()
        }
    }

    private fun cropImage() {
        if (mUri != null) {
            UCrop.of(mUri!!, cachedUri!!)
                    .start(this)
        }
    }

    private fun requestCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (intent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            mUri = contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
            startActivityForResult(intent, REQUEST_CODE_CAMERA)
        } else {
            setResult(GET_PHOTO_ERROR)
            finish()
        }
    }

    private fun requestPhotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, getIntent().getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_GALLERY)
        } else {
            setResult(GET_PHOTO_ERROR)
            finish()
        }
    }
    // delete if above method works well
//    private fun pickFromGallery() {
//        val intent = Intent()
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        // put to resources
//        startActivityForResult(Intent.createChooser(intent, "Select photo"), REQUEST_CODE_PICTURE)
//    }


    companion object {
        private const val PHOTO_INTENT_FLAG = "PHOTO_INTENT_FLAG"
        const val URI_PHOTO_RESULT = "URI_PHOTO_RESULT"
        const val GET_PHOTO_ERROR = 102
        const val REQUEST_CODE_GALLERY = 1
        const val REQUEST_CODE_CAMERA = 2

        fun start(flag: Int, activity: Activity): Intent {
            val intent = Intent(activity, CropActivity::class.java)
            intent.putExtra(PHOTO_INTENT_FLAG, flag)
            return intent
        }
    }

}
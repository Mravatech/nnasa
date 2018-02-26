package com.mnassa.other

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

        mUri = intent.getParcelableExtra(URI_PHOTO)

        val filename = System.currentTimeMillis().toString() + ".jpg"
        cachedUri = Uri.fromFile(File(cacheDir.toString() + "/" + filename))

        UCrop.of(mUri!!, cachedUri!!)
                .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            setResult(RESULT_OK, intent.putExtra(URI_PHOTO_RESULT, cachedUri))
            finish()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            setResult(CROP_PHOTO_ERROR)
            finish()
        }

    }

    companion object {
        private const val URI_PHOTO = "URI_PHOTO"
        const val URI_PHOTO_RESULT = "URI_PHOTO_RESULT"
        const val CROP_PHOTO_ERROR = 102
        const val CROP_PHOTO_CODE = 103

        fun start(uri: Uri, activity: Activity) : Intent {
            val intent = Intent(activity, CropActivity::class.java)
            intent.putExtra(URI_PHOTO, uri)
            return intent

        }
    }

}
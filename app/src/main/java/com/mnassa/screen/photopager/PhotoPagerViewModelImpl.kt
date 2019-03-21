package com.mnassa.screen.photopager

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.mnassa.R
import com.mnassa.core.addons.launchWorker
import com.mnassa.data.extensions.await
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Peter on 9/11/2018.
 */
class PhotoPagerViewModelImpl(
        private val appContext: Context,
        private val firebaseStorage: FirebaseStorage,
        private val exceptionHandler: ExceptionHandler
) : MnassaViewModelImpl(), PhotoPagerViewModel {

    override fun loadImage(imageUrl: String) {
        launchWorker {
            val uri = firebaseStorage.getReferenceFromUrl(imageUrl).downloadUrl.await(exceptionHandler)
                    ?: return@launchWorker
            val srcFileName = uri.lastPathSegment.substringAfterLast("/")
            val destFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + "/" + appContext.getString(R.string.app_name)

            val request = DownloadManager.Request(uri)

            request.setTitle(fromDictionary(R.string.save_to_gallery_download))
            request.setDescription(srcFileName)
            request.allowScanningByMediaScanner()

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            val dest = appContext.getString(R.string.app_name) + "/" + srcFileName

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, dest)
            val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            withContext(Dispatchers.Main) {
                Toast.makeText(appContext, fromDictionary(R.string.gallery_image_saved).format(destFolder), Toast.LENGTH_LONG).show()
            }
        }
    }
}
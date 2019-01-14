package com.mnassa.utils

import android.content.Context
import android.net.Uri
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * @author Artem Chepurnoy
 */
class FirebaseBigImageLoader(
    context: Context,
    okHttpClient: OkHttpClient?
) : GlideImageLoader(context, okHttpClient) {

    companion object {
        fun with(context: Context): FirebaseBigImageLoader {
            return with(context, null)
        }

        fun with(context: Context, okHttpClient: OkHttpClient?): FirebaseBigImageLoader {
            return FirebaseBigImageLoader(context, okHttpClient)
        }
    }

    private val requestTargetMap = ConcurrentHashMap<Int, Job>()

    override fun loadImage(requestId: Int, uri: Uri, callback: ImageLoader.Callback) {
        cancel(requestId)

        launch(UI) {
            val downloadUri: Uri
            try {
                downloadUri = withContext(DefaultDispatcher) { uri.downloadUri() }
            } catch (e: Exception) {
                callback.onFail(e)
                return@launch
            }

            super.loadImage(requestId, downloadUri, callback)
        }.also { job ->
            // Remember the job, so we can cancel it
            // later on if we need to.
            requestTargetMap[requestId] = job
        }
    }

    override fun prefetch(uri: Uri) {
        launch(UI) {
            val downloadUri: Uri
            try {
                downloadUri = withContext(DefaultDispatcher) { uri.downloadUri() }
            } catch (e: Exception) {
                return@launch
            }

            super.prefetch(downloadUri)
        }
    }

    override fun cancel(requestId: Int) {
        requestTargetMap.remove(requestId)
            ?.cancel()
        super.cancel(requestId)
    }

    /**
     * Gets the firebase storage downloadable [reference][Uri] to
     * of url.
     */
    private suspend fun Uri.downloadUri(): Uri {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(this.toString())
        return suspendCoroutine { continuation ->
            ref.downloadUrl
                .addOnSuccessListener(continuation::resume)
                .addOnFailureListener(continuation::resumeWithException)
        }
    }
}
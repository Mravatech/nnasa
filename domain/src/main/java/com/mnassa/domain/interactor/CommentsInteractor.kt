package com.mnassa.domain.interactor

import android.net.Uri
import com.mnassa.domain.repository.CommentsRepository

/**
 * Created by Peter on 3/23/2018.
 */
interface CommentsInteractor : CommentsRepository {
    suspend fun preloadCommentImage(image: Uri): String
}
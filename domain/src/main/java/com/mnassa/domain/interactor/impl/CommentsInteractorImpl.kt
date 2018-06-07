package com.mnassa.domain.interactor.impl

import android.net.Uri
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.model.FOLDER_COMMENTS
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.domain.repository.CommentsRepository

/**
 * Created by Peter on 3/23/2018.
 */
class CommentsInteractorImpl(private val commentsRepository: CommentsRepository,
                             private val storageInteractor: StorageInteractor) : CommentsInteractor, CommentsRepository by commentsRepository {

    override suspend fun preloadCommentImage(image: Uri): String {
        return storageInteractor.sendImage(StoragePhotoDataImpl(image, FOLDER_COMMENTS))
    }
}
package com.mnassa.screen.profile.edit

import android.net.Uri
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */
class EditProfileViewModelImpl(
        private val tagInteractor: TagInteractor,
        private val storageInteractor: StorageInteractor ) : MnassaViewModelImpl(), EditProfileViewModel {


    override val tagChannel: BroadcastChannel<List<TagModel>> = BroadcastChannel(10)
    override val imageUploadedChannel: BroadcastChannel<String> = BroadcastChannel(10)

    private var sendPhotoJob: Job? = null
    override fun uploadPhotoToStorage(uri: Uri) {
        sendPhotoJob?.cancel()
        sendPhotoJob = handleException {
            val path = storageInteractor.sendAvatar(StoragePhotoDataImpl(uri, FOLDER_AVATARS))
            imageUploadedChannel.send(path)
            Timber.i(path)
        }
    }

    private var tagJob: Job? = null
    override fun getTagsByIds(ids: List<String>) {
//        tagJob = handleException {
//            tagInteractor.getTagsByIds(ids)
//        }
    }

    override suspend fun search(search: String): List<TagModel> {
        return tagInteractor.search(search)
    }
}
package com.mnassa.screen.profile.edit.company

import android.net.Uri
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
class EditCompanyProfileViewModelImpl (
        private val tagInteractor: TagInteractor,
        private val storageInteractor: StorageInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor) : MnassaViewModelImpl(), EditCompanyProfileViewModel {

    override val tagChannel: BroadcastChannel<EditCompanyProfileViewModel.TagCommand> = BroadcastChannel(10)
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
    override fun getTagsByIds(ids: List<String>?, isOffers: Boolean) {
        val tagIds = ids ?: return
        tagJob = handleException {
            val tags = tagInteractor.getTagsByIds(tagIds)
            if (isOffers) {
                tagChannel.send(EditCompanyProfileViewModel.TagCommand.TagOffers(tags))
            } else {
                tagChannel.send(EditCompanyProfileViewModel.TagCommand.TagInterests(tags))
            }
        }
    }

    override suspend fun search(search: String): List<TagModel> {
        return tagInteractor.search(search)
    }


    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderInteractor.getReqieredPlaces(constraint)
    }
}
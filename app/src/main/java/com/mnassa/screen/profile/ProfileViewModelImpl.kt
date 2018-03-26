package com.mnassa.screen.profile

import android.net.Uri
import android.os.Bundle
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mnassa.domain.interactor.OtherProfileInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.screen.profile.model.ProfileModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
class ProfileViewModelImpl(
        private val storageInteractor: StorageInteractor,
        private val storage: FirebaseStorage,
        private val tagInteractor: TagInteractor,
        private val profileInteractor: OtherProfileInteractor) : MnassaViewModelImpl(), ProfileViewModel {

    override val imageUploadedChannel: BroadcastChannel<StorageReference> = BroadcastChannel(10)
    override val profileChannel: BroadcastChannel<ProfileModel> = BroadcastChannel(10)
    override val tagChannel: BroadcastChannel<List<TagModel>> = BroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleException {
            withProgressSuspend {
                val profileAccountModel = profileInteractor.getPrifileByAccountId("-L7iL1VRfulD0PIQBT7V"//"-L8CtC3Vst4AcfsP67lf"-L7iL1VRfulD0PIQBT7V
                )
                Timber.i(profileAccountModel.toString())
                if (profileAccountModel != null) {
                    val profile = ProfileModel(profileAccountModel,
                            tagInteractor.getTagsByIds(profileAccountModel.interests
                                    ?: emptyList()),
                            tagInteractor.getTagsByIds(profileAccountModel.offers
                                    ?: emptyList()))
                    profileChannel.send(profile)
                }

            }
        }
    }

    private var sendPhotoJob: Job? = null
    override fun uploadPhotoToStorage(uri: Uri) {
        sendPhotoJob?.cancel()
        sendPhotoJob = handleException {
            val path = storageInteractor.sendAvatar(StoragePhotoDataImpl(uri, FOLDER_AVATARS))
            imageUploadedChannel.send(storage.getReferenceFromUrl(path))
            Timber.i(path)
        }
    }


}
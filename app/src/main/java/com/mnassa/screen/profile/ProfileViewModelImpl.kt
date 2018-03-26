package com.mnassa.screen.profile

import android.net.Uri
import android.os.Bundle
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.OtherProfileInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
class ProfileViewModelImpl(
        private val storageInteractor: StorageInteractor,
        private val storage: FirebaseStorage,
        private val tagInteractor: UserProfileInteractor,
        private val profileInteractor: OtherProfileInteractor,
        private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), ProfileViewModel {

    override val imageUploadedChannel: BroadcastChannel<StorageReference> = BroadcastChannel(10)
    override val allConnectionsCountChannel: BroadcastChannel<Int> = BroadcastChannel(10)
    override val profileChannel: BroadcastChannel<ShortAccountModel> = BroadcastChannel(10)
    override val tagChannel: BroadcastChannel<List<TagModel>> = BroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        ShortAccountModel
//        handleException {//todo remove
//            delay(1000)
////                val v = tagInteractor.()
//                val x = mutableListOf<TagModel>()
//                for(z in 0..10){
//                    x.add(TagModelImpl("bla $z", "bla $z", "bla $z"))
//                }
//                tagChannel.send(x)
//
//        }
        handleException {
            withProgressSuspend {
                val v = profileInteractor.getPrifileByAccountId("-L7iL1VRfulD0PIQBT7V"//"-L8CtC3Vst4AcfsP67lf"-L7iL1VRfulD0PIQBT7V
                )
                Timber.i(v.toString())
                if (v != null)
                    profileChannel.send(v)
            }
        }
        handleException {
            connectionsInteractor.getConnectedConnections().consumeEach {
                allConnectionsCountChannel.send(it.size)
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
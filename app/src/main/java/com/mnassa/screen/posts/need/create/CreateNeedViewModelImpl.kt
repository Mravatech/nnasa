package com.mnassa.screen.posts.need.create

import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.repository.UserRepository
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/19/2018.
 */
class CreateNeedViewModelImpl(
        private val postId: String?,
        private val postsInteractor: PostsInteractor,
        private val tagInteractor: TagInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor,
        private val userRepository: UserRepository
) : MnassaViewModelImpl(), CreateNeedViewModel {

    override val closeScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)

    override fun createPost(
            need: String,
            tags: List<TagModel>,
            images: List<AttachedImage>,
            placeId: String?,
            price: Long?,
            timeOfExpiration: Long?,
            postPrivacyOptions: PostPrivacyOptions
    ) {
        handleException {
            withProgressSuspend {
                if (postId == null) {
                    postsInteractor.createNeed(
                            text = need,
                            imagesToUpload = images.filterIsInstance<AttachedImage.LocalImage>().map { it.imageUri },
                            uploadedImages = images.filterIsInstance<AttachedImage.UploadedImage>().map { it.imageUrl },
                            privacy = postPrivacyOptions,
                            tags = tags,
                            price = price,
                            timeOfExpiration = timeOfExpiration,
                            placeId = placeId
                    )
                } else {
                    postsInteractor.updateNeed(
                            postId = postId,
                            text = need,
                            imagesToUpload = images.filterIsInstance<AttachedImage.LocalImage>().map { it.imageUri },
                            uploadedImages = images.filterIsInstance<AttachedImage.UploadedImage>().map { it.imageUrl },
                            tags = tags,
                            price = price,
                            placeId = placeId
                    )
                }

                closeScreenChannel.send(Unit)
            }
        }
    }

    override suspend fun getDefaultExpirationDays(): Long = postsInteractor.getDefaultExpirationDays()

    override suspend fun getUser(userId: String): ShortAccountModel? = handleExceptionsSuspend { userRepository.getAccountById(userId) }

    override suspend fun getTag(tagId: String): TagModel? = tagInteractor.get(tagId)

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderInteractor.getReqieredPlaces(constraint)
    }

    override suspend fun search(search: String): List<TagModel> = tagInteractor.search(search)
}
package com.mnassa.screen.posts.need.details

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.ExpirationType
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 3/19/2018.
 */
interface NeedDetailsViewModel : MnassaViewModel {
    val postChannel: BroadcastChannel<PostModel>
    val postTagsChannel: BroadcastChannel<List<TagModel>>
    val finishScreenChannel: BroadcastChannel<Unit>

    suspend fun retrieveComplaints(): List<TranslatedWordModel>

    fun delete()
    fun repost(sharingOptions: PostPrivacyOptions)
    fun promote()
    fun sendComplaint(id: String, reason: String, authorText: String?)
    fun changeStatus(status: ExpirationType)

    data class ViewModelParams(
            val postId: String,
            val postAuthorId: String,
            val post: PostModel?)
}
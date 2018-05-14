package com.mnassa.screen.posts.need.details

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.data.network.NetworkContract
import com.mnassa.domain.interactor.ComplaintInteractor
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.model.impl.ComplaintModelImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 3/19/2018.
 */
open class NeedDetailsViewModelImpl(
        private val postId: String,
        private val postsInteractor: PostsInteractor,
        private val tagInteractor: TagInteractor,
        private val complaintInteractor: ComplaintInteractor
) : MnassaViewModelImpl(), NeedDetailsViewModel {
    override val postChannel: ConflatedBroadcastChannel<PostModel> = ConflatedBroadcastChannel()
    override val postTagsChannel: ConflatedBroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val finishScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)
    private var reportsList = emptyList<TranslatedWordModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleException {
            postsInteractor.loadById(postId).consumeEach {
                if (it != null) {
                    it.timeOfExpiration = getExpiration(it)
                    postChannel.send(it)
                    postTagsChannel.send(loadTags(it.tags))
                } else {
                    finishScreenChannel.send(Unit)
                }
            }
        }
        handleException {
            reportsList = complaintInteractor.getReports()
        }
    }

    private suspend fun getExpiration(post: PostModel): Date {
        if (post.timeOfExpiration != null) return requireNotNull(post.timeOfExpiration)
        val defaultDaysToExpire = postsInteractor.getDefaultExpirationDays()
        return Date(post.originalCreatedAt.time + TimeUnit.MILLISECONDS.convert(defaultDaysToExpire, TimeUnit.DAYS))
    }

    override suspend fun retrieveComplaints(): List<TranslatedWordModel> {
        if (reportsList.isNotEmpty()) return reportsList
        showProgress()
        reportsList = complaintInteractor.getReports()
        hideProgress()
        return reportsList
    }


    override fun delete() {
        handleException {
            withProgressSuspend {
                postsInteractor.removePost(postId)
                finishScreenChannel.send(Unit)
            }
        }
    }

    override fun repost(sharingOptions: PostPrivacyOptions) {
        handleException {
            withProgressSuspend {
                postsInteractor.repostPost(postId, null, sharingOptions)
            }
        }
    }

    override fun sendComplaint(id: String, reason: String, authorText: String?) {
        handleException {
            withProgressSuspend {
                complaintInteractor.sendComplaint(ComplaintModelImpl(
                        id = id,
                        type = NetworkContract.Complaint.POST_TYPE,
                        reason = reason,
                        authorText = authorText
                ))
            }
            finishScreenChannel.send(Unit)
        }
    }

    override fun promote() {
        handleException {
            withProgressSuspend {
                postsInteractor.promote(postChannel.consume { receive() })
            }
        }
    }

    private suspend fun loadTags(tags: List<String>): List<TagModel> {
        return tags.map { tag -> asyncWorker { tagInteractor.get(tag) } }.mapNotNull { it.await() }
    }
}
package com.mnassa.screen.posts.profile.create

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.addons.launchUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.*
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.lengthOrZero
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.posts.need.sharing.format
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_recommend_user.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/12/2018.
 */
class RecommendUserController(args: Bundle) : MnassaControllerImpl<RecommendUserViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_recommend_user
    private val recommendedUser by lazy { args[EXTRA_ACCOUNT] as ShortAccountModel }
    private val postId: String? by lazy { args.getString(EXTRA_POST_ID, null) }
    private val groupIds by lazy { args.getStringArrayList(EXTRA_GROUP_ID) ?: listOf<String>() }
    override val viewModel: RecommendUserViewModel by instance(arg = postId)
    private var waitForResumeJob: Job? = null
    override var sharingOptions = getSharingOptions(args)
        set(value) {
            field = value

            waitForResumeJob?.cancel()
            waitForResumeJob = GlobalScope.launchUI {
                getViewSuspend().tvShareOptions?.text = value.format()
            }
        }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.recommend_publish_button)) {
                view.toolbar.actionButtonClickable = false
                launchCoroutineUI {
                    viewModel.applyChanges(RawRecommendPostModel(
                            postId = postId,
                            groupIds = groupIds.toSet(),
                            privacy = sharingOptions,
                            text = etRecommend.text.toString(),
                            accountId = recommendedUser.id
                    ))
                }.invokeOnCompletion { onNeedTextUpdated() }
            }
            tvShareOptions.setOnClickListener {
                if (groupIds.isNotEmpty()) return@setOnClickListener

                launchCoroutineUI {
                    open(SharingOptionsController.newInstance(
                            options = sharingOptions,
                            listener = this@RecommendUserController,
                            accountsToExclude = listOf(recommendedUser.id),
                            canBePromoted = viewModel.canPromotePost(),
                            restrictShareReduction = postId != null,
                            promotePrice = viewModel.getPromotePostPrice()))
                }
            }

            launchCoroutineUI {
                tvShareOptions.text = sharingOptions.format()
            }
            etRecommend.prefix = "${fromDictionary(R.string.recommend_prefix)}  "
            etRecommend.hint = fromDictionary(R.string.recommend_message_placeholder)
            etRecommend.addTextChangedListener(SimpleTextWatcher { onNeedTextUpdated() })
            onNeedTextUpdated()

            if (args.containsKey(EXTRA_POST_TO_EDIT)) {
                val post = args.getSerializable(EXTRA_POST_TO_EDIT) as RecommendedProfilePostModel
                etRecommend.setText(post.text)
                args.remove(EXTRA_POST_TO_EDIT)
            }
        }

        launchCoroutineUI {
            viewModel.closeScreenChannel.consumeEach { close() }
        }
    }

    private fun onNeedTextUpdated() {
        view?.toolbar?.actionButtonClickable = canCreatePost()
    }

    private fun canCreatePost(): Boolean {
        return with(view ?: return false) {
            etRecommend.text.lengthOrZero >= MIN_TEXT_LENGTH
        }
    }

    companion object {
        private const val MIN_TEXT_LENGTH = 0
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
        private const val EXTRA_POST_ID = "EXTRA_POST_ID"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val EXTRA_POST_TO_EDIT = "EXTRA_POST_TO_EDIT"

        fun newInstance(account: ShortAccountModel, group: GroupModel? = null): RecommendUserController {
            val args = Bundle()
            args.putSerializable(EXTRA_ACCOUNT, account)
            group?.let {
                args.putSerializable(EXTRA_GROUP, it)
                args.putStringArrayList(EXTRA_GROUP_ID, arrayListOf(it.id))
            }

            return RecommendUserController(args)
        }

        fun newInstance(post: RecommendedProfilePostModel): RecommendUserController {
            val args = Bundle()
            args.putSerializable(EXTRA_ACCOUNT, post.recommendedProfile)
            args.putString(EXTRA_POST_ID, post.id)
            args.putStringArrayList(EXTRA_GROUP_ID, post.groupIds.toCollection(ArrayList()))
            args.putSerializable(EXTRA_POST_TO_EDIT, post)

            return RecommendUserController(args)
        }

        private fun getSharingOptions(args: Bundle): PostPrivacyOptions {
            return when {
                args.containsKey(EXTRA_GROUP) -> {
                    val group = args.getSerializable(EXTRA_GROUP) as GroupModel
                    PostPrivacyOptions(PostPrivacyType.PUBLIC(), emptySet(), setOf(group.id))
                }
                else -> PostPrivacyOptions.DEFAULT
            }
        }
    }
}
package com.mnassa.screen.posts.profile.create

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.model.RecommendedProfilePostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_recommend_user.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/12/2018.
 */
class RecommendUserController(args: Bundle) : MnassaControllerImpl<RecommendUserViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_recommend_user
    private val recommendedUser by lazy { args[EXTRA_ACCOUNT] as ShortAccountModel }
    private val postId: String? by lazy { args.getString(EXTRA_POST_ID, null) }
    override val viewModel: RecommendUserViewModel by instance(arg = postId)
    private var waitForResumeJob: Job? = null
    override var sharingOptions = SharingOptionsController.ShareToOptions.DEFAULT
        set(value) {
            field = value

            waitForResumeJob?.cancel()
            waitForResumeJob = launchCoroutineUI {
                lifecycle.awaitFirst { it == Lifecycle.Event.ON_RESUME }
                view?.tvShareOptions?.text = value.format()
            }
        }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.recommend_publish_button)) {
                viewModel.createPost(
                        postPrivacyOptions = sharingOptions.asPostPrivacy,
                        text = etRecommend.text.toString(),
                        recommendedUser = recommendedUser
                )
            }
            tvShareOptions.setOnClickListener {
                open(SharingOptionsController.newInstance(
                        options = sharingOptions,
                        listener = this@RecommendUserController,
                        accountsToExclude = listOf(recommendedUser.id)))
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
        val view = view ?: return
        view.toolbar.actionButtonEnabled = view.etRecommend.text.length >= MIN_TEXT_LENGTH
    }

    companion object {
        private const val MIN_TEXT_LENGTH = 0
        private const val MAX_SHARE_TO_USERNAMES = 2
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
        private const val EXTRA_POST_ID = "EXTRA_POST_ID"
        private const val EXTRA_POST_TO_EDIT = "EXTRA_POST_TO_EDIT"

        fun newInstance(account: ShortAccountModel): RecommendUserController {
            val args = Bundle()
            args.putSerializable(EXTRA_ACCOUNT, account)

            return RecommendUserController(args)
        }

        fun newInstance(post: RecommendedProfilePostModel): RecommendUserController {
            val args = Bundle()
            args.putSerializable(EXTRA_ACCOUNT, post.recommendedProfile)
            args.putString(EXTRA_POST_ID, post.id)
            args.putSerializable(EXTRA_POST_TO_EDIT, post)

            return RecommendUserController(args)
        }
    }
}
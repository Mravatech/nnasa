package com.mnassa.screen.posts.profile.create

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
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
    override var sharingOptions = SharingOptionsController.ShareToOptions.EMPTY
        set(value) {
            field = value

            waitForResumeJob?.cancel()
            waitForResumeJob = launchCoroutineUI {
                lifecycle.awaitFirst { it == Lifecycle.Event.ON_RESUME }
                view?.tvShareOptions?.text = formatShareToOptions(value)
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
                open(SharingOptionsController.newInstance(sharingOptions, this@RecommendUserController))
            }

            launchCoroutineUI {
                tvShareOptions.text = formatShareToOptions(sharingOptions)
            }
            etRecommend.prefix = "${fromDictionary(R.string.recommend_prefix)}  "
            etRecommend.hint = fromDictionary(R.string.recommend_message_placeholder)
            etRecommend.addTextChangedListener(SimpleTextWatcher { onNeedTextUpdated() })
            onNeedTextUpdated()
        }

        launchCoroutineUI {
            viewModel.closeScreenChannel.consumeEach { close() }
        }
    }

    private suspend fun formatShareToOptions(options: SharingOptionsController.ShareToOptions): String {
        with(options) {
            return fromDictionary(R.string.need_create_share_to_prefix).format(
                    when {
                        isPromoted -> fromDictionary(R.string.need_create_to_all_mnassa)
                        isMyNewsFeedSelected -> fromDictionary(R.string.need_create_to_newsfeed)
                        selectedConnections.isNotEmpty() -> {
                            val usernames = options.selectedConnections.take(MAX_SHARE_TO_USERNAMES).mapNotNull { viewModel.getUser(it) }.joinToString { it.userName }
                            if (selectedConnections.size <= 2) {
                                usernames
                            } else {
                                val tail = fromDictionary(R.string.need_create_to_connections_other).format(options.selectedConnections.size - 2)
                                "$usernames $tail"
                            }
                        }
                        else -> throw IllegalStateException()
                    }
            )
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

        fun newInstance(account: ShortAccountModel): RecommendUserController {
            val args = Bundle()
            args.putSerializable(EXTRA_ACCOUNT, account)

            return RecommendUserController(args)
        }
    }
}
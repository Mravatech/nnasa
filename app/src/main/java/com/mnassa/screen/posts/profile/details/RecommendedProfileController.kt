package com.mnassa.screen.posts.profile.details

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.asReference
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.MnassaToolbar
import kotlinx.android.synthetic.main.controller_need_details_header.view.*
import kotlinx.android.synthetic.main.recommended_profile.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/10/2018.
 */
class RecommendedProfileController(args: Bundle) : NeedDetailsController(args) {

    override val viewModel: RecommendedProfileViewModel by instance(arg = postId)

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            btnConnectNow.visibility = View.VISIBLE
        }
        val viewRef = view.asReference()
        launchCoroutineUI {
            viewModel.connectionStatusChannel.consumeEach { connectionStatus ->
                with(viewRef.invoke()) {
                    val post = btnConnectNow.tag as? RecommendedProfilePostModel
                            ?: return@with
                    if (connectionStatus.canBeConnected) {
                        btnConnectNow.text = fromDictionary(R.string.recommend_connect_now)
                        btnConnectNow.setOnClickListener { viewModel.connect(post.recommendedProfile) }
                    } else {
                        btnConnectNow.text = fromDictionary(R.string.recommend_open_profile)
                        btnConnectNow.setOnClickListener { open(ProfileController.newInstance(post.recommendedProfile)) }
                    }
                }
            }
        }

    }

    override suspend fun bindPost(post: PostModel) {
        super.bindPost(post)

        if (post is RecommendedProfilePostModel) {
            super.bindTags(post.offers)
            with(getViewSuspend()) {
                rlRecommendedProfileRoot.visibility = View.VISIBLE
                ivRecommendedUserAvatar.avatarSquare(post.recommendedProfile.avatar)
                ivRecommendedUserAvatar.setOnClickListener { open(ProfileController.newInstance(post.recommendedProfile)) }
                tvRecommendedUserName.text = post.recommendedProfile.formattedName
                tvRecommendedUserPosition.text = post.recommendedProfile.formattedPosition
                tvRecommendedUserPosition.goneIfEmpty()

                btnConnectNow.tag = post
            }
        }
    }

    override fun bindToolbar(toolbar: MnassaToolbar) {
        super.bindToolbar(toolbar)
        toolbar.title = fromDictionary(R.string.recommend_title)
    }

    override suspend fun bindTags(tags: List<TagModel>) = Unit
    override suspend fun makePostActionsVisible() = makePostActionsGone()

    companion object {
        //to create instance, use PostDetailsFactory
    }
}
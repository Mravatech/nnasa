package com.mnassa.screen.posts.profile.details

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_need_details.view.*
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

        headerLayout.invoke {
            with(it) {
                btnConnectNow.visibility = View.VISIBLE
            }
        }
        launchCoroutineUI {
            viewModel.connectionStatusChannel.consumeEach { connectionStatus ->
                headerLayout.invoke {
                    with(it) {
                        val post = btnConnectNow.tag as? RecommendedProfilePostModel
                                ?: return@invoke
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

    }

    override fun bindPost(post: PostModel) {
        super.bindPost(post)

        view?.toolbar?.title = fromDictionary(R.string.recommend_title)

        if (post is RecommendedProfilePostModel) {
            super.bindTags(post.offers)
            headerLayout.invoke {
                with(it) {
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
    }

    override fun bindTags(tags: List<TagModel>) = Unit
    override fun makePostActionsVisible() = makePostActionsGone()

    companion object {
        //to create instance, use PostDetailsFactory
    }
}
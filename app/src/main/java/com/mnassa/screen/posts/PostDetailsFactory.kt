package com.mnassa.screen.posts

import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.mnassa.App
import com.mnassa.core.addons.launchWorker
import com.mnassa.di.getInstance
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostType
import com.mnassa.extensions.isMyPost
import com.mnassa.extensions.markAsOpened
import com.mnassa.screen.comments.CommentsRewardModel
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.screen.invite.InviteSource
import com.mnassa.screen.invite.InviteSourceHolder
import com.mnassa.screen.posts.general.details.GeneralPostController
import com.mnassa.screen.posts.info.details.InfoDetailsController
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.screen.posts.offer.details.OfferDetailsController
import com.mnassa.screen.posts.profile.details.RecommendedProfileController

/**
 * Created by Peter on 4/11/2018.
 */
class PostDetailsFactory {

    fun newInstance(post: PostModel): Controller {

        launchWorker {
            post.markAsOpened()
        }

        App.context.getInstance<InviteSourceHolder>().source = InviteSource.Post(post)

        val args = Bundle()
        args.putString(EXTRA_POST_ID, post.id)
        args.putString(EXTRA_POST_AUTHOR_ID, post.repostAuthor?.id ?: post.author.id)
        args.putSerializable(EXTRA_POST_MODEL, post)

        val postController = when (post.type) {
            is PostType.PROFILE -> RecommendedProfileController(args)
            is PostType.GENERAL -> GeneralPostController(args)
            is PostType.INFO -> return InfoDetailsController.newInstance(post)
            is PostType.OFFER -> OfferDetailsController(args)
            else -> NeedDetailsController(args)
        }
        return CommentsWrapperController.newInstance(postController, CommentsRewardModel(true, post.isMyPost()))
    }

    companion object {
        const val EXTRA_POST_ID = "EXTRA_POST_ID"
        const val EXTRA_POST_MODEL = "EXTRA_POST_MODEL"
        const val EXTRA_POST_AUTHOR_ID = "EXTRA_POST_AUTHOR_ID"
    }

}
package com.mnassa.screen.posts

import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostType
import com.mnassa.extensions.markAsOpened
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.screen.posts.general.details.GeneralPostController
import com.mnassa.screen.posts.info.details.InfoDetailsController
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.screen.posts.offer.details.OfferDetailsController
import com.mnassa.screen.posts.profile.details.RecommendedProfileController
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

/**
 * Created by Peter on 4/11/2018.
 */
class PostDetailsFactory {



    fun newInstance(post: PostModel): Controller {

        launch {
            try {
                post.markAsOpened()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        val args = Bundle()
        args.putString(EXTRA_POST_ID, post.id)
        args.putSerializable(EXTRA_POST_MODEL, post)

        val postController = when (post.type) {
            PostType.PROFILE -> RecommendedProfileController(args)
            PostType.GENERAL -> GeneralPostController(args)
            PostType.INFO -> return InfoDetailsController.newInstance(post)
            PostType.OFFER -> OfferDetailsController(args)
            else -> NeedDetailsController(args)
        }
        return CommentsWrapperController.newInstance(postController)
    }

    companion object {
        const val EXTRA_POST_ID = "EXTRA_POST_ID"
        const val EXTRA_POST_MODEL = "EXTRA_POST_MODEL"
    }

}
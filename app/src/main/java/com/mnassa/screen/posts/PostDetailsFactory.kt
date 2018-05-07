package com.mnassa.screen.posts

import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostType
import com.mnassa.extensions.markAsOpened
import com.mnassa.extensions.isMyPost
import com.mnassa.screen.comments.CommentsRewardModel
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.screen.posts.general.details.GeneralPostController
import com.mnassa.screen.posts.info.details.InfoDetailsController
import com.mnassa.screen.posts.need.details.NeedDetailsController
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
        args.putString(NeedDetailsController.EXTRA_NEED_ID, post.id)
        args.putSerializable(NeedDetailsController.EXTRA_NEED_MODEL, post)

        val postController = when (post.type) {
            PostType.PROFILE -> RecommendedProfileController(args)
            PostType.GENERAL -> GeneralPostController(args)
            PostType.INFO -> return InfoDetailsController.newInstance(post)
            else -> NeedDetailsController(args)
        }
        return CommentsWrapperController.newInstance(postController, CommentsRewardModel(true, post.isMyPost()))
    }

}
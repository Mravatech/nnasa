package com.mnassa.screen.posts

import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostType
import com.mnassa.extensions.isMyPost
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.screen.posts.general.details.GeneralPostController
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.screen.posts.profile.details.RecommendedProfileController

/**
 * Created by Peter on 4/11/2018.
 */
class PostDetailsFactory {

    fun newInstance(post: PostModel): Controller {
        val args = Bundle()
        args.putString(NeedDetailsController.EXTRA_NEED_ID, post.id)
        args.putSerializable(NeedDetailsController.EXTRA_NEED_MODEL, post)

        val postController = when (post.type) {
            PostType.PROFILE -> RecommendedProfileController(args)
            PostType.GENERAL -> GeneralPostController(args)
            else -> NeedDetailsController(args)
        }
        return CommentsWrapperController.newInstance(postController, post.isMyPost())
    }

}
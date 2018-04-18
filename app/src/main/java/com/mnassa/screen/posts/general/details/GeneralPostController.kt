package com.mnassa.screen.posts.general.details

import android.os.Bundle
import com.mnassa.R
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.formattedName
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_need_details.view.*
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/13/2018.
 */
class GeneralPostController(args: Bundle) : NeedDetailsController(args) {
    override val viewModel: GeneralPostViewModel by instance(arg = postId)

    override fun bindPost(post: PostModel) {
        super.bindPost(post)

        view?.toolbar?.title = "${post.author.formattedName} ${fromDictionary(R.string.general_post_title)}"
    }

    override fun makePostActionsVisible() = makePostActionsGone()

    companion object {
        //to create instance, use PostDetailsFactory
    }
}
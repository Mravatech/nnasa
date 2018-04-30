package com.mnassa.screen.posts.general.details

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.formattedName
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.posts.general.create.CreateGeneralPostController
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.MnassaToolbar
import kotlinx.coroutines.experimental.channels.consume
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/13/2018.
 */
class GeneralPostController(args: Bundle) : NeedDetailsController(args) {
    override val viewModel: GeneralPostViewModel by instance(arg = postId)
    private val popupMenuHelper: PopupMenuHelper by instance()

    override fun bindToolbar(toolbar: MnassaToolbar) {
        super.bindToolbar(toolbar)
        launchCoroutineUI {
            val post = viewModel.postChannel.openSubscription().consume { receive() }
            toolbar.title = "${post.author.formattedName} ${fromDictionary(R.string.general_post_title)}"
        }
    }

    override fun showMyPostMenu(view: View, post: PostModel) {
        popupMenuHelper.showMyPostMenu(
                view = view,
                onEditPost = { open(CreateGeneralPostController.newInstance(post)) },
                onDeletePost = { viewModel.delete() })
    }

    override suspend fun makePostActionsVisible() = makePostActionsGone()

    companion object {
        //to create instance, use PostDetailsFactory
    }
}
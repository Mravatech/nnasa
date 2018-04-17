package com.mnassa.screen.comments

import com.mnassa.domain.model.CommentModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/17/2018.
 */
class CommentsWrapperViewModelImpl : MnassaViewModelImpl(), CommentsWrapperViewModel {
    override val scrollToChannel: BroadcastChannel<CommentModel>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val commentsChannel: BroadcastChannel<List<CommentModel>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val canReadCommentsChannel: BroadcastChannel<Boolean>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val canWriteCommentsChannel: BroadcastChannel<Boolean>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun createComment(text: String, accountsToRecommend: List<String>, replyTo: CommentModel?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editComment(originalComment: CommentModel, text: String, accountsToRecommend: List<String>, replyTo: CommentModel?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteComment(commentModel: CommentModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
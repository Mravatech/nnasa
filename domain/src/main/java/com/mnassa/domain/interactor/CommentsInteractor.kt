package com.mnassa.domain.interactor

import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.repository.CommentsRepository

/**
 * Created by Peter on 3/23/2018.
 */
interface CommentsInteractor : CommentsRepository {
    override suspend fun getCommentsByPost(postId: String): List<CommentModel>
}
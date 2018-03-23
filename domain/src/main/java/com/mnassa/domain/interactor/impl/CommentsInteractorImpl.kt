package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.repository.CommentsRepository

/**
 * Created by Peter on 3/23/2018.
 */
class CommentsInteractorImpl(private val commentsRepository: CommentsRepository) : CommentsInteractor, CommentsRepository by commentsRepository {
}
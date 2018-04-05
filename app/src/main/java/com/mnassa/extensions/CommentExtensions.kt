package com.mnassa.extensions

import com.mnassa.App
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.CommentModel

/**
 * Created by Peter on 3/27/2018.
 */
suspend fun CommentModel.isMyComment(): Boolean {
    return creator.id == App.context.getInstance<UserProfileInteractor>().getAccountId()
}
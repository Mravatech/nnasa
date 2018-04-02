package com.mnassa.extensions

import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mnassa.App
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.CommentModel

/**
 * Created by Peter on 3/27/2018.
 */
suspend fun CommentModel.isMyComment(): Boolean {
    return creator.id == App.context.appKodein().instance<UserProfileInteractor>().getAccountId()
}
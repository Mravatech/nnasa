package com.mnassa.extensions

import com.mnassa.App
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 5/14/2018.
 */
val GroupModel.formattedRole: String
    get() =
        if (isAdmin) fromDictionary(R.string.group_is_admin)
        else fromDictionary(R.string.group_is_member)

val GroupModel.formattedType: CharSequence get() = ""
val GroupModel.formattedName: CharSequence get() = name

fun GroupModel.isMyGroup(): Boolean {
    return creator.id == App.context.getInstance<UserProfileInteractor>().getAccountIdOrNull()
}

val GroupModel.isAdmin: Boolean get() = admins.contains(App.context.getInstance<UserProfileInteractor>().getAccountIdOrNull())
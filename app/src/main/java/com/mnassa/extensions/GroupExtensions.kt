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
fun GroupModel.formattedRole(isMember: Boolean? = null): String {
    return when {
        isAdmin -> fromDictionary(R.string.group_is_admin)
        (isMember ?: true) -> fromDictionary(R.string.group_is_member)
        else -> fromDictionary(R.string.group_not_member)
    }
}

val GroupModel.formattedType: String get() = ""
val GroupModel.formattedName: String get() = name.replace("\n", "").replace("\r", "")

fun GroupModel.isMyGroup(): Boolean {
    return creator?.id == App.context.getInstance<UserProfileInteractor>().getAccountIdOrNull()
}

val GroupModel.isAdmin: Boolean get() = admins.contains(App.context.getInstance<UserProfileInteractor>().getAccountIdOrNull())
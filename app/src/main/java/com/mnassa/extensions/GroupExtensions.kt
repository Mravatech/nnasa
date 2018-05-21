package com.mnassa.extensions

import com.mnassa.App
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.GroupType

/**
 * Created by Peter on 5/14/2018.
 */
val GroupType.formatted: String get() = when(this) {

    is GroupType.Public -> "Public" //TODO: use dictionary
    is GroupType.Private -> "Private"
}

val GroupModel.formattedType: CharSequence get() = type.formatted
val GroupModel.formattedName: CharSequence get() = name

fun GroupModel.isMyGroup(): Boolean {
    return creator.id == App.context.getInstance<UserProfileInteractor>().getAccountIdOrNull()
}
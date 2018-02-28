package com.mnassa.domain.model.impl

import com.mnassa.domain.model.UserProfileModel

/**
 * Created by Peter on 2/21/2018.
 */
data class UserProfileModelImpl(override var id: String, override val name: String) : UserProfileModel {
}
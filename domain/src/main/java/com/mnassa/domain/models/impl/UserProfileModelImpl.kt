package com.mnassa.domain.models.impl

import com.mnassa.domain.models.UserProfileModel

/**
 * Created by Peter on 2/21/2018.
 */
data class UserProfileModelImpl(override val id: String, override val name: String) : UserProfileModel {
}
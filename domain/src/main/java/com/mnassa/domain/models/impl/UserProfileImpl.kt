package com.mnassa.domain.models.impl

import com.mnassa.domain.models.UserProfile

/**
 * Created by Peter on 2/21/2018.
 */
data class UserProfileImpl(override val id: String, override val name: String) : UserProfile {
}
package com.mnassa.screen.profile.model

import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */

data class ProfileModel(
        val profile: ProfileAccountModel,
        val interests: List<TagModel>?,
        val offers: List<TagModel>?
)



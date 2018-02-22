package com.mnassa.domain.models

/**
 * Created by Peter on 2/21/2018.
 */
interface UserProfileModel : Model {
    override var id: String
    val name: String
}
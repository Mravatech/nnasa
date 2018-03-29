package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by Peter on 3/5/2018.
 */
interface AccountAbility : Serializable {
    val isMain: Boolean
    val name: String?
    val place: String?
}
package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by Peter on 2/22/2018.
 */
interface Model : Serializable, HasId {
    override var id: String
}
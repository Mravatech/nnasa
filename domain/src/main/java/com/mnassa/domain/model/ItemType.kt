package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by Peter on 4/16/2018.
 */
sealed class ItemType : Serializable {
    object ORIGINAL: ItemType()
    object REPOST: ItemType()
}
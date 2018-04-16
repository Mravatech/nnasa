package com.mnassa.domain.model

import java.io.Serializable
import java.util.*

/**
 * Created by Peter on 4/13/2018.
 */
interface EventModel : Model {
    val author: ShortAccountModel
    val commentsCount: Int
    val viewsCount: Int
    val createdAt: Date
    val duration: EventDuration
    val startAt: Date
    val location: LocationPlaceModel?

}


interface EventDuration : Serializable {
    val type: String
    val value: Long
}
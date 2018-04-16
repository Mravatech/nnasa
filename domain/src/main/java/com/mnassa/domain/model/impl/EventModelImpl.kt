package com.mnassa.domain.model.impl

import com.mnassa.domain.model.EventDuration
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.LocationPlaceModel
import com.mnassa.domain.model.ShortAccountModel
import java.util.*

/**
 * Created by Peter on 4/13/2018.
 */
data class EventModelImpl(
        override var id: String,
        override val author: ShortAccountModel,
        override val commentsCount: Int,
        override val viewsCount: Int,
        override val createdAt: Date,
        override val duration: EventDuration,
        override val startAt: Date,
        override val location: LocationPlaceModel?

) : EventModel {

}
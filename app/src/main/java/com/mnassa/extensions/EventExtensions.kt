package com.mnassa.extensions

import com.mnassa.R
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventType
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 4/17/2018.
 */
val EventModel.formattedType: CharSequence
    get() = when (type) {
        EventType.LECTURE -> fromDictionary(R.string.event_lecture)
        EventType.DISCUSSION -> fromDictionary(R.string.event_discussion)
        EventType.WORKSHOP -> fromDictionary(R.string.event_workshop)
        EventType.EXERCISE -> fromDictionary(R.string.event_exercise)
        EventType.ACTIVITY -> fromDictionary(R.string.event_activity)
    }
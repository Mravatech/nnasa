package com.mnassa.screen.events.details.info

import com.mnassa.domain.model.EventModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 4/18/2018.
 */
interface EventDetailsInfoViewModel : MnassaViewModel {
    val eventChannel: BroadcastChannel<EventModel>
}
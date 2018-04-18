package com.mnassa.screen.events.details.info

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.extensions.formatted
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_event_details_info.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat

/**
 * Created by Peter on 4/18/2018.
 */
class EventDetailsInfoController(args: Bundle) : MnassaControllerImpl<EventDetailsInfoViewModel>(args), CommentsWrapperController.CommentsWrapperCallback {
    private val eventId by lazy { args.getString(EXTRA_EVENT_ID) }
    override val layoutId: Int = R.layout.controller_event_details_info
    override val viewModel: EventDetailsInfoViewModel by instance(arg = eventId)
    private val languageProvider: LanguageProvider by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        launchCoroutineUI {
            viewModel.eventChannel.consumeEach { bindEvent(it) }
        }
    }



    private suspend fun bindEvent(event: EventModel) {
        with(getViewSuspend()) {
            val startTimeFormat = SimpleDateFormat("dd MMMM yyyy\nhh:mm a", languageProvider.locale)
            tvSchedule.text = startTimeFormat.format(event.startAt)
            tvSchedule.append("\n")
            tvSchedule.append(fromDictionary(R.string.event_duration))
            tvSchedule.append(" ")
            tvSchedule.append(event.duration.formatted)
            tvLocation.text = event.locationType.formatted
        }
    }

    override suspend fun getCommentInputContainer(self: CommentsWrapperController): ViewGroup {
        val parent = parentController
        return if (parent is CommentsWrapperController.CommentInputContainer)
            parent.getCommentInputContainer(self)
        else super.getCommentInputContainer(self)
    }

    companion object {
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"

        fun newInstance(eventId: String): EventDetailsInfoController {
            val args = Bundle()
            args.putString(EXTRA_EVENT_ID, eventId)
            return EventDetailsInfoController(args)
        }
    }
}
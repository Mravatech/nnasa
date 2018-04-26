package com.mnassa.screen.events.details

import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.bindDate
import com.mnassa.extensions.image
import com.mnassa.extensions.isInvisible
import com.mnassa.extensions.isMyEvent
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.screen.events.create.CreateEventController
import com.mnassa.screen.events.details.info.EventDetailsInfoController
import com.mnassa.screen.events.details.participants.EventDetailsParticipantsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_event_details.view.*
import kotlinx.android.synthetic.main.event_date.view.*
import kotlinx.coroutines.experimental.runBlocking
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/17/2018.
 */
class EventDetailsController(args: Bundle) : MnassaControllerImpl<EventDetailsViewModel>(args), CommentsWrapperController.CommentInputContainer, MnassaRouter {
    private val eventId by lazy { args.getString(EXTRA_EVENT_ID) }
    override val layoutId: Int = R.layout.controller_event_details
    override val viewModel: EventDetailsViewModel by instance(arg = eventId)
    private val popupMenuHelper: PopupMenuHelper by instance()
    private var eventModel: EventModel? = null
        get() {
            if (field == null) {
                field = args[EXTRA_EVENT] as EventModel?
            }
            return field
        }

    private val adapter: RouterPagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val page: Controller = when (position) {
                    EventPages.INFORMATION.ordinal -> CommentsWrapperController.newInstance(EventDetailsInfoController.newInstance(eventId, eventModel))
                    EventPages.PARTICIPANTS.ordinal -> EventDetailsParticipantsController.newInstance(eventId, eventModel)
                    else -> throw IllegalArgumentException("Invalid page position $position")
                }
                router.setRoot(RouterTransaction.with(page))
            }
        }

        override fun getCount(): Int = EventPages.values().size

        override fun getPageTitle(position: Int): CharSequence = when (position) {
            EventPages.INFORMATION.ordinal -> fromDictionary(R.string.event_tab_info)
            EventPages.PARTICIPANTS.ordinal -> fromDictionary(R.string.event_tab_participants)
            else -> throw IllegalArgumentException("Invalid page position $position")
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tlEventTabs.setupWithViewPager(vpEvents)
            vpEvents.adapter = adapter

            toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
            toolbar.inflateMenu(R.menu.event_menu)
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.actionShowMenu) {
                    onMenuClick(toolbar)
                }
                true
            }

            appBarLayout.addOnOffsetChangedListener(offsetChangedListener)
        }

        runBlocking { eventModel?.apply { bindEvent(this) } }
    }

    override suspend fun getCommentInputContainer(self: CommentsWrapperController): ViewGroup {
        return getViewSuspend().commentInputContainer
    }

    private fun onMenuClick(view: View) {
        val event = eventModel ?: return
        if (event.isMyEvent()) {
            popupMenuHelper.showMyEventMenu(
                    view,
                    onChangeStatusClick = {},
                    onEditClick = { open(CreateEventController.newInstance(event)) })
        }
    }

    private suspend fun bindEvent(event: EventModel) {
        eventModel = event
        with(getViewSuspend()) {
            val mainImage = event.pictures.firstOrNull()
            ivEventImage.image(mainImage)
            ivEventImage.setOnClickListener {
                if (mainImage != null) PhotoPagerActivity.start(it.context, listOf(mainImage))
            }

            event.bindDate(llEventDateRoot)
            tvEventName.text = event.title
            tvTitleCollapsed.text = event.title

            val creatorText = SpannableStringBuilder(fromDictionary(R.string.event_details_by))
            val spanStart = creatorText.length
            creatorText.append(event.author.formattedName)
            creatorText.setSpan(StyleSpan(Typeface.BOLD), spanStart, creatorText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            tvEventCreator.text = creatorText
        }
    }

    private val offsetChangedListener: AppBarLayout.OnOffsetChangedListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        view?.tvTitleCollapsed?.isInvisible = Math.abs(verticalOffset) - appBarLayout.totalScrollRange != 0
    }

    enum class EventPages {
        INFORMATION,
        PARTICIPANTS
    }

    override fun open(self: Controller, controller: Controller) = mnassaRouter.open(this, controller)
    override fun close(self: Controller) = mnassaRouter.close(self)

    companion object {
        private const val EXTRA_EVENT = "EXTRA_EVENT"
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"

        fun newInstance(event: EventModel): EventDetailsController {
            val args = Bundle()
            args.putString(EXTRA_EVENT_ID, event.id)
            args.putSerializable(EXTRA_EVENT, event)
            return EventDetailsController(args)
        }
    }
}
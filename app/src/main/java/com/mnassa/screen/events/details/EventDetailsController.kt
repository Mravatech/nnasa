package com.mnassa.screen.events.details

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.di.getInstance
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.comments.CommentsRewardModel
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.screen.complaintother.ComplaintOtherController
import com.mnassa.screen.events.EventDetailsFactory.Companion.EXTRA_EVENT
import com.mnassa.screen.events.EventDetailsFactory.Companion.EXTRA_EVENT_ID
import com.mnassa.screen.events.create.CreateEventController
import com.mnassa.screen.events.details.info.EventDetailsInfoController
import com.mnassa.screen.events.details.participants.EventDetailsParticipantsController
import com.mnassa.screen.invite.InviteSource
import com.mnassa.screen.invite.InviteSourceHolder
import com.mnassa.screen.photopager.PhotoPagerController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_event_details.view.*
import kotlinx.android.synthetic.main.event_date.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/17/2018.
 */
class EventDetailsController(args: Bundle) : MnassaControllerImpl<EventDetailsViewModel>(args),
        CommentsWrapperController.CommentInputContainer,
        MnassaRouter,
        ComplaintOtherController.OnComplaintResult {
    private val eventId by lazy { args.getString(EXTRA_EVENT_ID) }
    override val layoutId: Int = R.layout.controller_event_details
    override val viewModel: EventDetailsViewModel by instance(arg = eventId)
    private val dialogHelper: DialogHelper by instance()
    private var eventModel: EventModel = args[EXTRA_EVENT] as EventModel

    override var onComplaint: String = ""
        set(value) {
            viewModel.sendComplaint(eventId, OTHER, value)
        }

    private val adapter: RouterPagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val page: Controller = when (position) {
                    EventPages.INFORMATION.ordinal -> {
                        val eventInfo = EventDetailsInfoController.newInstance(eventId, eventModel)
                        CommentsWrapperController.newInstance(
                                eventInfo,
                                CommentsRewardModel(canReward = false, isOwner = false))
                    }
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

        requireNotNull(applicationContext).getInstance<InviteSourceHolder>().source = InviteSource.Event(eventModel)

        with(view) {
            tlEventTabs.setupWithViewPager(vpEvents)
            vpEvents.adapter = adapter
            vpEvents.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) { hideKeyboard() }
            })
            toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
            appBarLayout.addOnOffsetChangedListener(offsetChangedListener)
        }

        launchCoroutineUI {
            viewModel.finishScreenChannel.consumeEach { close() }
        }

        launchCoroutineUI {
            viewModel.eventChannel.consumeEach { bindEvent(it) }
        }

        launchCoroutineUI { eventModel.apply { bindEvent(this) } }
    }

    override suspend fun getCommentInputContainer(self: CommentsWrapperController): ViewGroup = getViewSuspend().commentInputContainer
    override fun getCommentInputContainerNullable(self: CommentsWrapperController): ViewGroup? = view?.commentInputContainer

    private suspend fun bindEvent(event: EventModel) {
        eventModel = event
        initToolbarMenu(event)
        with(getViewSuspend()) {
            val mainImage = event.pictures.firstOrNull()
            ivEventImage.image(mainImage)
            ivEventImage.setOnClickListener {
                if (mainImage != null) open(PhotoPagerController.newInstance(listOf(mainImage)))
            }

            event.bindDate(llEventDateRoot)
            tvEventName.text = event.title
            tvTitleCollapsed.text = event.title

            val creatorText = SpannableStringBuilder(fromDictionary(R.string.event_details_by))
            val spanStart = creatorText.length
            creatorText.append(event.author.formattedName)
            creatorText.setSpan(StyleSpan(Typeface.BOLD), spanStart, creatorText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            tvEventCreator.text = creatorText
            tvEventCreator.setOnClickListener {
                open(ProfileController.newInstance(event.author))
            }
        }
    }

    private suspend fun initToolbarMenu(event: EventModel) {
        val toolbar = getViewSuspend().toolbar

        toolbar.menu?.clear()
        if (event.isMyEvent()) {
            toolbar.inflateMenu(R.menu.event_edit)
        } else {
            toolbar.inflateMenu(R.menu.event_view)
        }

        toolbar.menu.apply {
            findItem(R.id.action_event_edit)?.title = fromDictionary(R.string.event_menu_edit)
            findItem(R.id.action_event_change_status)?.title = fromDictionary(R.string.event_menu_change_status)
            findItem(R.id.action_event_promote)?.title = fromDictionary(R.string.event_promote_menu)
            findItem(R.id.action_event_report)?.title = fromDictionary(R.string.need_action_report)
        }

        if (!event.canBePromoted()) {
            toolbar.menu.removeItem(R.id.action_event_promote)
        }

        val promotionPrice = event.getPromotionPrice()

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_event_edit -> event.let { open(CreateEventController.newInstance(it)) }
                R.id.action_event_change_status -> {
                    dialogHelper.selectEventStatusDialog(toolbar.context, event.status) { status ->
                        event.let { event -> viewModel.changeStatus(event, status) }
                    }
                }
                R.id.action_event_report -> complainAboutProfile()
                R.id.action_event_promote -> {
                    dialogHelper.showConfirmPostPromotingDialog(toolbar.context, promotionPrice) {
                        viewModel.promote()
                    }
                }
            }
            true
        }
    }

    private val offsetChangedListener: AppBarLayout.OnOffsetChangedListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        view?.tvTitleCollapsed?.isInvisible = Math.abs(verticalOffset) - appBarLayout.totalScrollRange != 0
    }

    private fun complainAboutProfile() {
        launchCoroutineUI {
            val reportsList = viewModel.retrieveComplaints()
            dialogHelper.showComplaintDialog(getViewSuspend().context, reportsList) {
                if (it.id == OTHER) {
                    val controller = ComplaintOtherController.newInstance()
                    controller.targetController = this@EventDetailsController
                    open(controller)
                } else {
                    viewModel.sendComplaint(eventId, it.id, null)
                }
            }
        }
    }

    enum class EventPages {
        INFORMATION,
        PARTICIPANTS
    }

    override fun open(self: Controller, controller: Controller) = mnassaRouter.open(this, controller)
    override fun close(self: Controller) = mnassaRouter.close(self)

    companion object {
        private const val OTHER = "other"
    }
}
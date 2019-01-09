package com.mnassa.screen.events.details.info

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.screen.photopager.PhotoPagerController
import com.mnassa.screen.posts.need.details.adapter.PhotoPagerAdapter
import com.mnassa.screen.posts.need.details.adapter.PostTagRVAdapter
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
    private var event =  args[EXTRA_EVENT] as EventModel
    override val layoutId: Int = R.layout.controller_event_details_info
    override val viewModel: EventDetailsInfoViewModel by instance(arg = eventId)
    private val languageProvider: LanguageProvider by instance()
    private val dialogHelper: DialogHelper by instance()
    private val tagsAdapter = PostTagRVAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvTags.layoutManager = ChipsLayoutManager.newBuilder(context)
                    .setScrollingEnabled(false)
                    .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()
            rvTags.adapter = tagsAdapter

            btnBuyTickets.text = formatBuyButtonText(event, true, 0)
            btnBuyTickets.isEnabled = false
        }

        bindEvent(event, null, view)

        launchCoroutineUI {
            viewModel.eventChannel.consumeEach {
                bindEvent(event = it)
            }
        }
        launchCoroutineUI {
            viewModel.boughtTicketsChannel.consumeEach {
                bindEvent(tickets = it)
            }
        }
    }

    override fun onDestroyView(view: View) {
        view.rvTags.adapter = null
        super.onDestroyView(view)
    }

    private suspend fun bindEvent(
        event: EventModel = viewModel.eventChannel.valueOrNull ?: this.event,
        tickets: List<EventTicketModel>? = viewModel.boughtTicketsChannel.valueOrNull
    ) {
        bindEvent(event, tickets, getViewSuspend())
    }

    private fun bindEvent(event: EventModel, boughtTickets: List<EventTicketModel>?, view: View) {
        with(view) {
            tvSchedule.text = formatTime(event)
            //
            tvLocation.text = event.locationType.formatted
            //
            tvType.text = event.type.formatted
            //
            tvDescription.text = event.text
            //
            val pictures = if (event.pictures.size > 1) event.pictures.takeLast(event.pictures.size - 1) else emptyList()
            if (pictures.isNotEmpty()) {
                pivImages.count = pictures.size
                pivImages.selection = 0

                vpImages.adapter = PhotoPagerAdapter(pictures) {
                    open(PhotoPagerController.newInstance(pictures, pictures.indexOf(it)))
                }
                vpImages.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        pivImages.selection = position
                    }
                })
            }
            flImages.isGone = pictures.isEmpty()
            //
            tvViewsCount.text = fromDictionary(R.string.need_views_count).format(event.viewsCount)
            //
            tvCommentsCount.setHeaderWithCounter(R.string.need_comments_count, event.commentsCount)
            //
            llEventLocation.setOnClickListener { openGoogleMaps(event, it.context) }

            bindBuyTicketButton(event, boughtTickets)
            launchCoroutineUI { bindTags(viewModel.loadTags(event.tags)) }
        }
    }

    private fun bindBuyTicketButton(event: EventModel, boughtTickets: List<EventTicketModel>?) {
        with(view!!) {
            val boughtTicketsCountNullable = boughtTickets?.sumBy { it.ticketCount.toInt() }?.toLong()
            val boughtTicketsCount = boughtTicketsCountNullable ?: 0L
            val canBuyTickets = boughtTicketsCountNullable?.let(event::canBuyTickets) ?: false
            //
            tvTickets.text = formatTicketsText(event, context, boughtTicketsCountNullable)

            // buy button logic
            btnBuyTickets.isEnabled = canBuyTickets
            btnBuyTickets.text = formatBuyButtonText(event, canBuyTickets, boughtTicketsCount)
            btnBuyTickets.setBackgroundResource(if (boughtTicketsCount == 0L) R.drawable.btn_main else R.drawable.btn_green)
            btnBuyTickets.setOnClickListener { view ->
                launchCoroutineUI {
                    viewModel.buyTickets(dialogHelper.showBuyTicketDialog(view.context, event))
                }
            }
        }
    }

    private suspend fun bindTags(tags: List<TagModel>) {
        tagsAdapter.set(tags)
        getViewSuspend().let {
            with(it) {
                vTagsSeparator.isGone = tags.isEmpty()
                rvTags.isGone = tags.isEmpty()
            }
        }
    }

    private fun formatTime(event: EventModel): CharSequence {
        val startTimeFormat = SimpleDateFormat("dd MMMM yyyy\nhh:mm a", languageProvider.locale)
        val scheduleText = SpannableStringBuilder(startTimeFormat.format(event.startAt))
        scheduleText.append("\n")
        scheduleText.append(fromDictionary(R.string.event_duration))
        scheduleText.append(": ")
        scheduleText.append(event.duration?.formatted
                ?: fromDictionary(R.string.event_duration_not_specified))
        return scheduleText
    }

    private fun formatTicketsText(event: EventModel, context: Context, boughtTicketsCount: Long?): CharSequence {
        val ticketsText = SpannableStringBuilder()
        ticketsText.append(fromDictionary(R.string.event_price_template).format(event.price))
        ticketsText.append("\n")
        val greenSectionStart = ticketsText.length
        ticketsText.append(fromDictionary(R.string.event_tickets).format(event.ticketsTotal - event.ticketsSold))
        ticketsText.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.green_cool)), greenSectionStart, ticketsText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ticketsText.append(fromDictionary(R.string.event_tickets_from).format(event.ticketsTotal))
        ticketsText.append("\n")
        val blueSpanStart = ticketsText.length
        if (boughtTicketsCount != null) {
            ticketsText.append(fromDictionary(R.string.event_tickets).format(boughtTicketsCount))
            ticketsText.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.accent
                    )
                ), blueSpanStart, ticketsText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ticketsText.append(" ")
            ticketsText.append(fromDictionary(R.string.event_tickets_bought_by_me))
        }
        return ticketsText
    }

    private fun formatBuyButtonText(event: EventModel, canBuyTickets: Boolean, boughtTicketsCount: Long): CharSequence {
        val buyTicketsText = SpannableStringBuilder()
        buyTicketsText.append(when {
            canBuyTickets && boughtTicketsCount == 0L -> fromDictionary(R.string.event_buy_button_active)
            canBuyTickets && boughtTicketsCount > 0L -> fromDictionary(R.string.event_tickets_buy_more)
            event.status is EventStatus.ANNULED -> fromDictionary(R.string.event_tickets_buy_annulled)
            event.status is EventStatus.CLOSED -> fromDictionary(R.string.event_tickets_buy_closed)
            event.status is EventStatus.SUSPENDED -> fromDictionary(R.string.event_tickets_buy_suspended)
            event.ticketsSold >= event.ticketsTotal -> fromDictionary(R.string.event_tickets_buy_sold_out)
            boughtTicketsCount >= event.ticketsPerAccount -> fromDictionary(R.string.event_buy_button_reached_limit)
            else -> ""
        })

        if (canBuyTickets) {
            buyTicketsText.append(" ")
            buyTicketsText.append(
                    if (event.isFree) fromDictionary(R.string.event_buy_button_free_suffix)
                    else fromDictionary(R.string.event_price_template).format(event.price))
        }
        return buyTicketsText
    }

    private fun openGoogleMaps(event: EventModel, context: Context) {
        val location = event.locationType
        if (location is EventLocationType.Specified) {
            location.location?.let {
                val gmmIntentUri = Uri.parse("geo:${it.lat},${it.lng}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.`package` = "com.google.android.apps.maps"
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    startActivity(mapIntent)
                }
            }
        }
    }

    override suspend fun getCommentInputContainer(self: CommentsWrapperController): ViewGroup {
        val parent = parentController
        return if (parent is CommentsWrapperController.CommentInputContainer)
            parent.getCommentInputContainer(self)
        else super.getCommentInputContainer(self)
    }

    override fun getCommentInputContainerNullable(self: CommentsWrapperController): ViewGroup? {
        val parent = parentController
        return if (parent is CommentsWrapperController.CommentInputContainer)
            parent.getCommentInputContainerNullable(self)
        else super.getCommentInputContainerNullable(self)
    }

    companion object {
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        const val EXTRA_EVENT = "EXTRA_EVENT"

        fun newInstance(eventId: String, event: EventModel): EventDetailsInfoController {
            val args = Bundle()
            args.putString(EXTRA_EVENT_ID, eventId)
            args.putSerializable(EXTRA_EVENT, event)
            return EventDetailsInfoController(args)
        }
    }
}
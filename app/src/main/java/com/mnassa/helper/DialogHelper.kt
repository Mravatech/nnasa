package com.mnassa.helper

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatRadioButton
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.mnassa.BuildConfig
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventStatus
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.extensions.formatted
import com.mnassa.extensions.getBoughtTicketsCount
import com.mnassa.screen.invite.InviteController.Companion.INVITE_WITH_SHARE
import com.mnassa.screen.invite.InviteController.Companion.INVITE_WITH_SMS
import com.mnassa.screen.invite.InviteController.Companion.INVITE_WITH_WHATS_APP
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.dialog_buy_ticket.view.*
import kotlinx.android.synthetic.main.dialog_company_status.*
import kotlinx.android.synthetic.main.dialog_delete_chat_message.*
import kotlinx.android.synthetic.main.dialog_invite_with.*
import kotlinx.android.synthetic.main.dialog_occupation.*
import kotlinx.android.synthetic.main.dialog_welcome.view.*
import kotlinx.android.synthetic.main.dialog_yes_no.*
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog
import java.util.*

class DialogHelper {

    fun showSelectImageSourceDialog(context: Context, listener: (CropActivity.ImageSource) -> Unit) {
        MaterialDialog.Builder(context)
                .items(
                        fromDictionary(R.string.image_source_gallery),
                        fromDictionary(R.string.image_source_camera)
                )
                .itemsCallback { dialog, _, which, _ ->
                    listener(CropActivity.ImageSource.values()[which])
                    dialog.dismiss()
                }
                .cancelable(true)
                .show()
    }

    fun showComplaintDialog(context: Context, reports: List<TranslatedWordModel>, listener: (TranslatedWordModel) -> Unit) {
        MaterialDialog.Builder(context)
                .items(reports)
                .itemsCallback { dialog, _, which, _ ->
                    listener(reports[which])
                    dialog.dismiss()
                }
                .cancelable(true)
                .show()
    }

    fun showWelcomeDialog(context: Context, onOkClick: () -> Unit) {
        showSuccessDialog(
                context,
                fromDictionary(R.string.welcome_dialog_title),
                fromDictionary(R.string.welcome_dialog_description),
                onOkClick)
    }

    fun showDeleteMessageDialog(
            context: Context,
            isMyMessageClicked: Boolean,
            onDeleteForMeClick: () -> Unit,
            onDeleteForBothClick: () -> Unit,
            onCopyClick: () -> Unit,
            onReplyClick: () -> Unit) {
        val dialog = Dialog(context, R.style.DialogInvite)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_chat_message)
        if (isMyMessageClicked) {
            dialog.tvDeleteForBoth.text = fromDictionary(R.string.chats_delete_for_all)
            dialog.tvDeleteForBoth.setOnClickListener {
                onDeleteForBothClick()
                dialog.dismiss()
            }
            dialog.tvDeleteForBoth.visibility = View.VISIBLE
        } else {
            dialog.tvReply.text = fromDictionary(R.string.chats_reply)
            dialog.tvReply.setOnClickListener {
                onReplyClick()
                dialog.dismiss()
            }
            dialog.tvReply.visibility = View.VISIBLE
        }
        dialog.tvDeleteForMe.text = fromDictionary(R.string.chats_delete_for_me)
        dialog.tvCopyChatMessage.text = fromDictionary(R.string.chats_copy)
        dialog.tvCancel.text = fromDictionary(R.string.chats_cancel)
        dialog.tvDeleteForMe.setOnClickListener {
            onDeleteForMeClick()
            dialog.dismiss()
        }
        dialog.tvCopyChatMessage.setOnClickListener {
            onCopyClick()
            dialog.dismiss()
        }
        dialog.tvCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun showChooseOccupationDialog(context: Context,
                                   occupations: List<String>,
                                   position: Int,
                                   onSelectClick: (position: Int) -> Unit) {
        val dialog = Dialog(context, R.style.OccupationDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_occupation)
        fun closeDialogAfterClick(position: Int) {
            onSelectClick(position)
            dialog.dismiss()
        }
        dialog.tvOccupationHeader.text = fromDictionary(R.string.reg_dialog_header)
        for ((index, value) in occupations.withIndex()) {
            val radioButton = dialog.rOccupationContainer.getChildAt(index) as AppCompatRadioButton
            radioButton.text = value
            radioButton.setOnClickListener { closeDialogAfterClick(index) }
            radioButton.isChecked = position == index
        }
        dialog.show()
    }

    fun calendarDialog(context: Context, listener: DatePickerDialog.OnDateSetListener, calendar: Calendar = Calendar.getInstance()) {
        DatePickerDialog(
                context,
                listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun timeDialog(context: Context, listener: TimePickerDialog.OnTimeSetListener, calendar: Calendar = Calendar.getInstance()) {
        TimePickerDialog(
                context,
                listener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show()
    }

    fun durationDialog(context: Context, listener: TimeDurationPickerDialog.OnDurationSetListener, durationMillis: Long = 0) {
        TimeDurationPickerDialog(context, listener, durationMillis, TimeDurationPicker.HH_MM).show()
    }


    fun showSuccessDialog(context: Context, title: CharSequence, description: CharSequence, onOkClick: () -> Unit = {}) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_welcome, null)
        dialogView.tvTitle.text = title
        dialogView.tvDescription.text = description

        val dialog = MaterialDialog.Builder(context)
                .customView(dialogView, false)
                .cancelListener { onOkClick() }
                .show()

        dialogView.btnOk.setOnClickListener { dialog.cancel() }
        dialogView.btnOk.setText(android.R.string.ok)
    }

    fun showConfirmPostRemovingDialog(context: Context, onOkClick: () -> Unit) {
        MaterialDialog.Builder(context)
                .title(fromDictionary(R.string.post_delete_dialog_title))
                .content(fromDictionary(R.string.post_delete_dialog_description))
                .positiveText(fromDictionary(R.string.post_delete_dialog_yes))
                .negativeText(fromDictionary(R.string.post_delete_dialog_no))
                .onPositive { _, _ -> onOkClick() }
                .show()
    }

    fun showChooseCompanyStatusDialog(context: Context,
                                      statuses: List<String>,
                                      position: Int,
                                      onSelectClick: (position: Int) -> Unit) {
        val dialog = Dialog(context, R.style.OccupationDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_company_status)
        fun closeDialogAfterClick(position: Int) {
            onSelectClick(position)
            dialog.dismiss()
        }
        dialog.tvCompanyStatusHeader.text = fromDictionary(R.string.reg_company_status_label)
        for ((index, value) in statuses.withIndex()) {
            val radioButton = dialog.rCompanyStatusContainer.getChildAt(index) as AppCompatRadioButton
            radioButton.text = value
            radioButton.setOnClickListener { closeDialogAfterClick(index) }
            radioButton.isChecked = position == index
        }
        dialog.show()
    }

    fun yesNoDialog(context: Context, info: CharSequence, onOkClick: () -> Unit) {
        val dialog = Dialog(context, R.style.OccupationDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_yes_no)
        dialog.tvConnectionInfo.text = info
        dialog.tvYes.text = fromDictionary(R.string.user_profile_yes)
        dialog.tvNo.text = fromDictionary(R.string.user_profile_no)
        dialog.tvNo.setOnClickListener { dialog.dismiss() }
        dialog.tvYes.setOnClickListener {
            onOkClick()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun chooseSendInviteWith(context: Context, name: String?, isWhatsAppInstalled: Boolean, onInviteWithClick: (inviteWith: Int) -> Unit) {
        val dialog = Dialog(context, R.style.DialogInvite)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_invite_with)
        dialog.tvShortMessageService.text = fromDictionary(R.string.invite_invite_send_with_sms)
        dialog.tvMore.text = fromDictionary(R.string.invite_invite_send_with_more)
        dialog.tvWhatsApp.text = fromDictionary(R.string.invite_invite_send_with_whats_app)
        dialog.tvInviteDialogSubTitle.text = fromDictionary(R.string.invite_invite_select_way_to_send)
        dialog.tvInviteDialogTitle.text = name?.let { fromDictionary(R.string.invite_invite_you_invite).format(it) }
                ?: run { fromDictionary(R.string.invite_invite_you_invite_unknown_name) }
        fun sendInvite(inviteWith: Int) {
            onInviteWithClick(inviteWith)
            dialog.dismiss()
        }
        if (isWhatsAppInstalled) {
            dialog.llWhatsApp.setOnClickListener { sendInvite(INVITE_WITH_WHATS_APP) }
        }
        dialog.llShortMessageService.setOnClickListener { sendInvite(INVITE_WITH_SMS) }
        dialog.llMore.setOnClickListener { sendInvite(INVITE_WITH_SHARE) }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    fun showLoginByEmailDebugDialog(context: Context, listener: (email: String, password: String) -> Unit) {
        if (!BuildConfig.DEBUG) return
        //!!!DEBUG ONLY!!!
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL

        val email = EditText(context)
        email.hint = "Email"
        container.addView(email)

        val password = EditText(context)
        password.hint = "Password"
        container.addView(password)

        lateinit var dialog: AlertDialog

        var btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "p3@nxt.ru"
        btnHardcodedEmailAndPassword.setOnClickListener {
            listener("p3@nxt.ru", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)
        //
        btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "chas@ukr.net"
        btnHardcodedEmailAndPassword.setOnClickListener {
            listener("chas@ukr.net", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)
        //
        btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "serg@u.net"
        btnHardcodedEmailAndPassword.setOnClickListener {
            listener("serg@u.net", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)
        //
        btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "anton@u.net"
        btnHardcodedEmailAndPassword.setOnClickListener {
            listener("anton@u.net", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)

        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        container.layoutParams = layoutParams

        dialog = AlertDialog.Builder(context)
                .setView(container)
                .setPositiveButton("Login", { _, _ ->
                    listener(email.text.toString(),
                            password.text.toString())
                })
                .show()
    }

    fun showProgressDialog(context: Context): Dialog {
        val dialog = object : ProgressDialog(context, R.style.MnassaProgressTheme) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.dialog_progress)
            }
        }
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

    /**
     * Returns tickets count to buy
     */
    suspend fun showBuyTicketDialog(context: Context, event: EventModel): Long {
        val maxTicketsCount = minOf(event.ticketsTotal - event.ticketsSold, event.ticketsPerAccount - event.getBoughtTicketsCount())
        if (maxTicketsCount <= 0) return 0L

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_buy_ticket, null)
        return suspendCancellableCoroutine { continuation ->
            val dialog: MaterialDialog = MaterialDialog.Builder(context)
                    .customView(view, false)
                    .cancelListener { continuation.resume(0L) }
                    .build()

            with(view) {
                val setTotalPoints = { count: Long ->
                    val priceText = SpannableStringBuilder((count * event.price).toString())
                    priceText.setSpan(RelativeSizeSpan(1f), 0, priceText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    priceText.append(" ")
                    val spanStart = priceText.length
                    priceText.append(fromDictionary(R.string.event_tickets_buy_dialog_points))
                    priceText.setSpan(RelativeSizeSpan(0.4f), spanStart, priceText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    tvPrice.text = priceText
                }

                setTotalPoints(event.price)

                class PeopleCount(val count: Long) {
                    override fun toString(): String = "$count ${fromDictionary(R.string.event_tickets_buy_dialog_people)}"
                }

                val adapterData = (1..maxTicketsCount).map { PeopleCount(it) }
                spinnerQuantity.adapter = ArrayAdapter(context,
                        R.layout.support_simple_spinner_dropdown_item,
                        android.R.id.text1,
                        adapterData)
                spinnerQuantity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        setTotalPoints(adapterData[position].count)
                    }
                }

                btnBuyNow.setOnClickListener {
                    continuation.resume((spinnerQuantity.selectedItem as? PeopleCount)?.count ?: 0L)
                    dialog.dismiss()
                }
                btnBuyNow.text = fromDictionary(R.string.event_tickets_buy_dialog_buy_now)
                btnCancel.setOnClickListener { dialog.cancel() }
                btnCancel.text = fromDictionary(R.string.event_tickets_buy_dialog_cancel)
            }

            dialog.show()
            continuation.invokeOnCompletion { dialog.dismiss() }
        }
    }

    fun selectEventStatusDialog(context: Context, initStatus: EventStatus? = null, onStatusSelected: (EventStatus) -> Unit) {
        val statuses = listOf(EventStatus.ANNULED, EventStatus.OPENED, EventStatus.CLOSED, EventStatus.SUSPENDED)

        val index = if (initStatus == null) -1 else statuses.indexOfFirst { it::class.java == initStatus::class.java }

        MaterialDialog.Builder(context)
                .items(statuses.map { it.formatted })
                .itemsCallbackSingleChoice(index, { _, _, which, _ ->
                    onStatusSelected(statuses[which])
                    true
                })
                .show()
    }
}
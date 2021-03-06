package com.mnassa.screen.invite.history

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.extensions.avatarRound
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_invite_history.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */

class InviteHistoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun setup(contact: PhoneContactInvited, date: String?, showBottomShadow: Boolean) {
        date?.let {
            itemView.tvInviteHistoryHeader.visibility = View.VISIBLE
            itemView.vShadow.visibility = View.VISIBLE
            itemView.tvInviteHistoryHeader.text = fromDictionary(R.string.invite_invite_history_invited_in).format(it)
        } ?: run {
            itemView.tvInviteHistoryHeader.visibility = View.GONE
            itemView.vShadow.visibility = View.GONE
        }
        itemView.vShadowBottom.visibility = if (showBottomShadow) View.VISIBLE else View.GONE
        itemView.ivPhoneContactAvatar.avatarRound(contact.avatar)
        contact.description?.let {
            itemView.tvInviteContactName.visibility = View.VISIBLE
            itemView.tvInviteContactName.text = it
        } ?: kotlin.run {
            itemView.tvInviteContactName.visibility = View.GONE
        }
        itemView.tvInviteContactNumber.text = contact.phoneNumber
        if (contact.used) {
            itemView.tvRegistration.text = fromDictionary(R.string.invite_invite_history_registered)
            itemView.tvRegistration.setTextColor(ContextCompat.getColor(itemView.context, R.color.green_cool))
        } else {
            itemView.tvRegistration.text = fromDictionary(R.string.invite_invite_history_pending)
            itemView.tvRegistration.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_cool))
        }
    }
}
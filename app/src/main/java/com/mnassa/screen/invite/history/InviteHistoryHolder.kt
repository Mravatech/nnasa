package com.mnassa.screen.invite.history

import android.support.v7.widget.RecyclerView
import android.view.View
import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.extensions.avatarRoundWithStringPath
import kotlinx.android.synthetic.main.item_invite.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */

class InviteHistoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun setup(contact: PhoneContactInvited) {
        itemView.ivPhoneContactAvatar.avatarRoundWithStringPath(contact.avatar)
        itemView.tvInviteContactName.text = contact.description
        itemView.tvInviteContactNumber.text = contact.phoneNumber
    }
}
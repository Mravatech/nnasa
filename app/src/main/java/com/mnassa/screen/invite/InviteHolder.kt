package com.mnassa.screen.invite

import android.support.v7.widget.RecyclerView
import android.view.View
import com.mnassa.domain.model.PhoneContact
import com.mnassa.extensions.avatarRound
import kotlinx.android.synthetic.main.item_invite.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */

class InviteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun setup(contact: PhoneContact, viewModel: InviteViewModel) {
        itemView.ivPhoneContactAvatar.avatarRound(contact.avatar)
        itemView.tvInviteContactName.text = contact.fullName
        itemView.tvInviteContactNumber.text = contact.phoneNumber
        itemView.setOnClickListener { viewModel.selectPhoneContact(contact) }
    }

}
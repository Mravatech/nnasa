package com.mnassa.screen.invite

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PhoneContact
import com.mnassa.extensions.avatarRound
import kotlinx.android.synthetic.main.item_invite.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */

class InviteAdapter : RecyclerView.Adapter<InviteAdapter.InviteHolder>() {
    private var data: List<PhoneContact> = emptyList()
    var onItemClickListener = { item: PhoneContact -> }
    private var filtered: List<PhoneContact> = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            InviteHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_invite, parent, false))

    override fun getItemCount() = filtered.size

    override fun onBindViewHolder(holder: InviteHolder, position: Int) {
        holder.setup(filtered[position])
    }

    fun setData(data: List<PhoneContact>) {
        this.data = data
        filtered = data
        notifyDataSetChanged()
    }

    fun searchByNameOrNumber(text: String) {
        val query = text.toLowerCase().replace(" ", "")

        filtered = data.filter {
            it.fullName.toLowerCase().contains(query) || it.phoneNumber.replace(" ", "").contains(query)
        }
        notifyDataSetChanged()
    }

    fun getNameByNumber(text: String): String? {
        return data.firstOrNull { it.phoneNumber.replace(" ", "").endsWith(text) }?.fullName
    }

    inner class InviteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setup(contact: PhoneContact) {
            itemView.ivPhoneContactAvatar.avatarRound(contact.avatar)
            itemView.tvInviteContactName.text = contact.fullName
            itemView.tvInviteContactNumber.text = contact.phoneNumber
            itemView.setOnClickListener { onItemClickListener(contact) }
        }
    }

}
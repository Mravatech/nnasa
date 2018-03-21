package com.mnassa.screen.hail.history

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PhoneContactInvited

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */
class InviteHistoryAdapter(private val data: List<PhoneContactInvited>) : RecyclerView.Adapter<InviteHistoryHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            InviteHistoryHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_invite_history, parent, false))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: InviteHistoryHolder, position: Int) {
        holder.setup(data[position])
    }
}
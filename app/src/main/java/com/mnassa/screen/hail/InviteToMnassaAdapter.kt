package com.mnassa.screen.hail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PhoneContact

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */

class InviteToMnassaAdapter(
        private val data: List<PhoneContact>,
        private val viewModel: InviteToMnassaViewModel) : RecyclerView.Adapter<InviteToMnassaViewHolder>() {

    private var filtered: List<PhoneContact> = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            InviteToMnassaViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_invite, parent, false))

    override fun getItemCount() = filtered.size

    override fun onBindViewHolder(holder: InviteToMnassaViewHolder, position: Int) {
        holder.setup(filtered[position], viewModel)
    }

    fun searchByName(text: String) {
        filtered = data.filter { it.fullName.toLowerCase().startsWith(text.toLowerCase()) }
        notifyDataSetChanged()
    }

    fun searchByNumber(text: String) {
        filtered = data.filter { it.phoneNumber.startsWith(text) }
        notifyDataSetChanged()
    }

    fun getNameByNumber(text: String):String? {
        return data.firstOrNull { it.phoneNumber.endsWith(text) }?.fullName
    }

}
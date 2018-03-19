package com.mnassa.screen.hail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mnassa.R

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */

class InviteToMnassaAdapter(private val data: List<String>) : RecyclerView.Adapter<InviteToMnassaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            InviteToMnassaViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_invite, parent, false))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: InviteToMnassaViewHolder?, position: Int) {

    }

}
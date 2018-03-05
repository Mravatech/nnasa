package com.mnassa.screen.invite

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formettedName
import com.mnassa.extensions.avatar
import kotlinx.android.synthetic.main.item_invite_account.view.*

/**
 * Created by Peter on 3/5/2018.
 */
class InviteAdapter : RecyclerView.Adapter<InviteAdapter.InviteViewHolder>() {

    private val data: List<ShortAccountModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invite_account, parent, false)
        return InviteViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) = holder.bind(data[position])

    class InviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: ShortAccountModel) {
            with(itemView) {

                ivAvatar.avatar(data.avatar)

                tvUserName.text = data.formettedName
                tvUserPosition.text = ""
            }
        }
    }
}
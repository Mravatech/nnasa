package com.mnassa.screen.login.selectaccount

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import kotlinx.android.synthetic.main.item_select_account.view.*

/**
 * Created by Peter on 2/28/2018.
 */
//TODO: write and use base RV adapter
class AccountsRecyclerViewAdapter(private val data: MutableList<ShortAccountModel>) : RecyclerView.Adapter<AccountsRecyclerViewAdapter.AccountViewHolder>() {
    var onItemClickListener: (ShortAccountModel) -> Unit = {}

    fun setData(data: List<ShortAccountModel>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
        //TODO: Diff utils???
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_account, parent, false)
        val holder = AccountViewHolder(view)
        view.tag = holder

        view.setOnClickListener {
            val adapterPosition = (it.tag as AccountViewHolder).adapterPosition
            if (adapterPosition >= 0) {
                onItemClickListener(data[adapterPosition])
            }
        }
        return holder
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(data[position])
    }

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: ShortAccountModel) {
            with(itemView) {
                tvName.text = data.userName
            }
        }
    }
}
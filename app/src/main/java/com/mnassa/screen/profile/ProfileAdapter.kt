package com.mnassa.screen.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_profile_.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

class ProfileAdapter() : BasePaginationRVAdapter<String>() {

    private val selectedAccountsInternal: MutableList<String> = mutableListOf()
    var data: List<String>
        get() = selectedAccountsInternal
        set(value) {
            selectedAccountsInternal.clear()
            selectedAccountsInternal.addAll(value)

            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<String> {
        return ProfileViewHolder.newInstance(parent, selectedAccountsInternal)

    }

    private class ProfileViewHolder(private val selectedAccount: List<String>, itemView: View) : BaseVH<String>(itemView) {
        override fun bind(item: String) {
            with(itemView) {
                tvText.text = item
            }

        }

        companion object {
            fun newInstance(parent: ViewGroup, selectedAccount: List<String>): ProfileViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_, parent, false)
                val viewHolder = ProfileViewHolder(selectedAccount, view)
                return viewHolder
            }
        }

    }


}
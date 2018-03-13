package com.mnassa.screen.connections.adapters

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.mainAbility
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connections_all.view.*

/**
 * Created by Peter on 3/7/2018.
 */
class AllConnectionsRecyclerViewAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {
    var onItemClickListener = { account: ShortAccountModel, sender: View -> }
    var onBindHeader = { header: View -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> =
            UserViewHolder.newInstance(parent, this)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<ShortAccountModel> {
        return if (viewType == TYPE_HEADER) HeaderHolder.newInstance(parent) else
            super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseVH<ShortAccountModel>, position: Int) {
        if (holder is HeaderHolder) {
            onBindHeader(holder.itemView)
        } else super.onBindViewHolder(holder, position)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnMoreOptions -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onItemClickListener(getDataItemByAdapterPosition(position), v)
                }
            }
        }
    }

    private class HeaderHolder(itemView: View) : BaseVH<ShortAccountModel>(itemView) {
        override fun bind(item: ShortAccountModel) = Unit

        companion object {
            fun newInstance(parent: ViewGroup): HeaderHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.controller_connections_header, parent, false)
                return HeaderHolder(view)
            }
        }
    }

    private class UserViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<ShortAccountModel>(itemView) {

        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = item.mainAbility(fromDictionary(R.string.invite_at_placeholder))
                tvPosition.goneIfEmpty()

                tvEventName.text = formatEvent(item)
                tvEventName.goneIfEmpty()

                btnMoreOptions.setOnClickListener(clickListener)
                btnMoreOptions.tag = this@UserViewHolder
            }
        }

        private fun formatEvent(item: ShortAccountModel): CharSequence {
            if (/*event is null*/false) return ""

            val head = "From event " //TODO: from dictionary
            val spannable = SpannableString(head + "Some event name")
            val color = ContextCompat.getColor(requireNotNull(itemView.context), R.color.black)
            spannable.setSpan(ForegroundColorSpan(color), head.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }

        companion object {
            fun newInstance(parent: ViewGroup, clickListener: View.OnClickListener): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_all, parent, false)
                return UserViewHolder(view, clickListener)
            }
        }
    }
}
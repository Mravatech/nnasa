package com.mnassa.screen.connections.select

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedFromEvent
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_connections_all.view.*

/**
 * Created by Peter on 4/2/2018.
 */
class SelectConnectionRVAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {
    var onItemClickListener = { account: ShortAccountModel, sender: View -> }
    fun destroyCallbacks() {
        onItemClickListener = { _, _ -> }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        return UserViewHolder.newInstance(parent, this)
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.rvConnectionRoot -> onItemClickListener(getDataItemByAdapterPosition(position), view)
        }
    }

    private class UserViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<ShortAccountModel>(itemView) {

        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName

                tvPosition.text = item.formattedPosition
                tvPosition.goneIfEmpty()

                tvEventName.text = item.formattedFromEvent
                tvEventName.goneIfEmpty()

                rvConnectionRoot.setOnClickListener(clickListener)
                rvConnectionRoot.tag = this@UserViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, clickListener: View.OnClickListener): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_all, parent, false)
                view.btnMoreOptions.visibility = View.GONE
                return UserViewHolder(view, clickListener)
            }
        }
    }
}
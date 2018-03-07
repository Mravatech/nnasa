package com.mnassa.screen.connections

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.mainAbility
import com.mnassa.extensions.avatar
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connections_recommended.view.*

/**
 * Created by Peter on 3/7/2018.
 */
class RecommendedConnectionsRecyclerViewAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {
    private val data = ArrayList<ShortAccountModel>()
    var onConnectClickListener = { account: ShortAccountModel -> }

    override fun createView(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        return UserViewHolder.newInstance(parent, this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnConnect -> {
                val position = (v.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onConnectClickListener(getDataItemByAdapterPosition(position))
                }
            }
        }
    }

    private class UserViewHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<ShortAccountModel>(itemView) {

        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatar(item.avatar)
                tvUserName.text = item.formattedName
                tvPosition.text = item.mainAbility(fromDictionary(R.string.invite_at_placeholder))
                btnConnect.setOnClickListener(onClickListener)
                btnConnect.tag = this@UserViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_recommended, parent, false)
                return UserViewHolder(view, onClickListener)
            }
        }
    }
}
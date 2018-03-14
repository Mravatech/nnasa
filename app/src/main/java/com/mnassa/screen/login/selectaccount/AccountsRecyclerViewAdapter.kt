package com.mnassa.screen.login.selectaccount

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_select_account.view.*

/**
 * Created by Peter on 2/28/2018.
 */
class AccountsRecyclerViewAdapter : BasePaginationRVAdapter<ShortAccountModel>() {
    var onItemClickListener: (ShortAccountModel) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_account, parent, false)
        val holder = AccountViewHolder(view)
        view.rlRoot.tag = holder

        view.rlRoot.setOnClickListener {
            val adapterPosition = (it.tag as AccountViewHolder).adapterPosition
            if (adapterPosition >= 0) {
                onItemClickListener(getDataItemByAdapterPosition(adapterPosition))
            }
        }
        return holder
    }

    class AccountViewHolder(itemView: View) : BaseVH<ShortAccountModel>(itemView) {
        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName
                tvPosition.text = item.formattedPosition
                tvPosition.goneIfEmpty()
            }
        }
    }
}
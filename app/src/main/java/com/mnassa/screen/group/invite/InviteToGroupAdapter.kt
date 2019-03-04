package com.mnassa.screen.group.invite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedFromEvent
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_group_invite.view.*

/**
 * Created by Peter on 3/5/2018.
 */
class InviteToGroupAdapter : BasePaginationRVAdapter<UserInvite>(), View.OnClickListener {

    var onSendInviteClick: (item: ShortAccountModel) -> Unit = {}
    var onRevokeInviteClick: (item: ShortAccountModel) -> Unit = {}
    var onRemoveUserClick: (item: ShortAccountModel) -> Unit = {}

    override var filterPredicate: (item: UserInvite) -> Boolean = { it.user.formattedName.toLowerCase().contains(searchPhrase.toLowerCase()) }

    init {
        dataStorage = FilteredSortedDataStorage(filterPredicate, SimpleDataProviderImpl())
        searchListener = dataStorage as SearchListener<UserInvite>
    }

    fun searchByName(searchText: String) {
        searchPhrase = searchText
        searchListener.search()
    }

    fun destroyCallbacks() {
        onSendInviteClick = { }
        onRevokeInviteClick = { }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<UserInvite> {
        return InviteViewHolder.newInstance(parent, this)
    }

    override fun onClick(view: View) {
        val viewHolder = view.tag as? InviteViewHolder ?: return
        val position = viewHolder.adapterPosition

        if (position >= 0) {
            val item = getDataItemByAdapterPosition(position)
            when {
                item.isMember -> onRemoveUserClick(item.user)
                item.isInvited -> onRevokeInviteClick(item.user)
                else -> onSendInviteClick(item.user)
            }
        }
    }

    private class InviteViewHolder(itemView: View) : BaseVH<UserInvite>(itemView) {

        override fun bind(item: UserInvite) {
            with(itemView) {
                ivAvatar.avatarRound(item.user.avatar)
                tvUserName.text = item.user.formattedName
                tvPosition.text = item.user.formattedPosition
                tvPosition.goneIfEmpty()
                tvEventName.text = item.user.formattedFromEvent
                tvEventName.goneIfEmpty()

                when {
                    item.isMember -> {
                        btnInvite.text = fromDictionary(R.string.group_member_invite_remove)
                        btnInvite.setTextColor(ContextCompat.getColor(context, R.color.money_spent))
                    }
                    item.isInvited -> {
                        btnInvite.text = fromDictionary(R.string.group_member_invite_revoke)
                        btnInvite.setTextColor(ContextCompat.getColor(context, R.color.money_spent))
                    }
                    else -> {
                        btnInvite.text = fromDictionary(R.string.group_member_invite_send)
                        btnInvite.setTextColor(ContextCompat.getColor(context, R.color.money_gained))
                    }
                }

                btnInvite
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): InviteViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_invite, parent, false)

                val viewHolder = InviteViewHolder(view)
                view.tag = viewHolder
                view.btnInvite.tag = viewHolder

                view.setOnClickListener(onClickListener)
                view.btnInvite.setOnClickListener(onClickListener)

                return viewHolder
            }
        }
    }
}
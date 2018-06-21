package com.mnassa.screen.group.invite

import android.support.v4.content.ContextCompat
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
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_group_invite.view.*

/**
 * Created by Peter on 3/5/2018.
 */
class InviteToGroupAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {

    private val blockedSelectedUsers = HashSet<String>()
    var onSendInviteClick: (item: ShortAccountModel) -> Unit = {}
    var onRevokeInviteClick: (item: ShortAccountModel) -> Unit = {}

    override var filterPredicate: (item: ShortAccountModel) -> Boolean = { it.formattedName.toLowerCase().contains(searchPhrase.toLowerCase()) }

    init {
        dataStorage = FilteredSortedDataStorage(filterPredicate, SimpleDataProviderImpl())
        searchListener = dataStorage as SearchListener<ShortAccountModel>
    }

    fun searchByName(searchText: String) {
        searchPhrase = searchText
        searchListener.search()
    }

    fun destroyCallbacks() {
        onSendInviteClick = { }
        onRevokeInviteClick = { }
    }

    fun setNotUnselectableUsers(userIds: Set<String>) {
        blockedSelectedUsers.clear()
        blockedSelectedUsers.addAll(userIds)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        return InviteViewHolder.newInstance(parent, this, blockedSelectedUsers)
    }

    override fun onClick(view: View) {
        val viewHolder = view.tag as? InviteViewHolder ?: return
        val position = viewHolder.adapterPosition

        if (position >= 0) {
            val item = getDataItemByAdapterPosition(position)
            if (blockedSelectedUsers.contains(item.id)) {
                onRevokeInviteClick(item)
            } else onSendInviteClick(item)
        }
    }

    private class InviteViewHolder(
            private val blockedSelectedAccounts: Set<String>,
            itemView: View) : BaseVH<ShortAccountModel>(itemView) {

        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName
                tvPosition.text = item.formattedPosition
                tvPosition.goneIfEmpty()
                tvEventName.text = item.formattedFromEvent
                tvEventName.goneIfEmpty()

                val isAlreadyInvited = blockedSelectedAccounts.contains(item.id)
                if (isAlreadyInvited) {
                    btnInvite.text = fromDictionary(R.string.group_member_invite_revoke)
                    btnInvite.setTextColor(ContextCompat.getColor(context, R.color.money_spent))
                } else {
                    btnInvite.text = fromDictionary(R.string.group_member_invite_send)
                    btnInvite.setTextColor(ContextCompat.getColor(context, R.color.money_gained))
                }

                btnInvite
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener, blockedSelectedAccounts: Set<String>): InviteViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_invite, parent, false)

                val viewHolder = InviteViewHolder(blockedSelectedAccounts, view)
                view.tag = viewHolder
                view.btnInvite.tag = viewHolder

                view.setOnClickListener(onClickListener)
                view.btnInvite.setOnClickListener(onClickListener)

                return viewHolder
            }
        }
    }
}
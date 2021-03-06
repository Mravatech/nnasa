package com.mnassa.screen.buildnetwork

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
import kotlinx.android.synthetic.main.item_build_network.view.*

/**
 * Created by Peter on 3/5/2018.
 */
class BuildNetworkAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {

    private val blockedSelectedUsers = HashSet<String>()
    private val selectedAccountsInternal: MutableSet<String> = HashSet()
    var onSelectedAccountsChangedListener = { selectedAccountIds: Set<String> -> }
    var selectedAccounts: Set<String>
        get() = selectedAccountsInternal
        set(value) {
            selectedAccountsInternal.clear()
            selectedAccountsInternal.addAll(value)
            onSelectedAccountsChangedListener(selectedAccountsInternal)

            notifyDataSetChanged()
        }

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
        onSelectedAccountsChangedListener = { }
    }

    fun setNotUnselectableUsers(userIds: Set<String>) {
        blockedSelectedUsers.clear()
        blockedSelectedUsers.addAll(userIds)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        return InviteViewHolder.newInstance(parent, this, selectedAccountsInternal, blockedSelectedUsers)
    }

    override fun onClick(view: View) {
        val viewHolder = view.tag as? InviteViewHolder ?: return
        val position = viewHolder.adapterPosition

        if (position >= 0) {
            val item = getDataItemByAdapterPosition(position)

            if (selectedAccounts.contains(item.id)) {
                selectedAccountsInternal.remove(item.id)
            } else selectedAccountsInternal.add(item.id)
            viewHolder.bind(item)

            onSelectedAccountsChangedListener(selectedAccountsInternal)
        }
    }

    private class InviteViewHolder(
            private val selectedAccounts: Set<String>,
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
                cbInvite.isChecked = selectedAccounts.contains(item.id)
                if (blockedSelectedAccounts.contains(item.id)) {
                    cbInvite.isChecked = true
                    cbInvite.isEnabled = false
                } else {
                    cbInvite.isEnabled = true
                }
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener, selectedAccount: Set<String>, blockedSelectedAccounts: Set<String>): InviteViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_build_network, parent, false)

                val viewHolder = InviteViewHolder(selectedAccount, blockedSelectedAccounts, view)
                view.tag = viewHolder
                view.cbInvite.tag = viewHolder

                view.setOnClickListener(onClickListener)
                view.cbInvite.setOnClickListener(onClickListener)

                return viewHolder
            }
        }
    }
}
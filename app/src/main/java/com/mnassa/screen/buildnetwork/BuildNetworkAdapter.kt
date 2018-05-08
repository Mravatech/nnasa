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
import com.mnassa.screen.base.adapter.FilteredSortedDataStorage
import com.mnassa.screen.base.adapter.SearchListener
import kotlinx.android.synthetic.main.item_build_network.view.*

/**
 * Created by Peter on 3/5/2018.
 */
class BuildNetworkAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {

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
        dataStorage = FilteredSortedDataStorage(filterPredicate, SimpleDataProviderImpl(), this)
        searchListener = dataStorage as SearchListener
    }

    fun searchByName(searchText: String) {
        searchPhrase = searchText
        searchListener.search()
    }

    fun destroyCallbacks() {
        onSelectedAccountsChangedListener = { }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        return InviteViewHolder.newInstance(parent, this, selectedAccountsInternal)
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

    private class InviteViewHolder(private val selectedAccount: Set<String>, itemView: View) : BaseVH<ShortAccountModel>(itemView) {
        override fun bind(item: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatarRound(item.avatar)
                tvUserName.text = item.formattedName
                tvPosition.text = item.formattedPosition
                tvPosition.goneIfEmpty()
                tvEventName.text = item.formattedFromEvent
                tvEventName.goneIfEmpty()
                cbInvite.isChecked = selectedAccount.contains(item.id)
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener, selectedAccount: Set<String>): InviteViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_build_network, parent, false)

                val viewHolder = InviteViewHolder(selectedAccount, view)
                view.tag = viewHolder
                view.cbInvite.tag = viewHolder

                view.setOnClickListener(onClickListener)
                view.cbInvite.setOnClickListener(onClickListener)

                return viewHolder
            }
        }
    }
}
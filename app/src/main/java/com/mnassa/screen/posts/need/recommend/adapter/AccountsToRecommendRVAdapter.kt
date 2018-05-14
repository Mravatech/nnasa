package com.mnassa.screen.posts.need.recommend.adapter

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
import kotlinx.android.synthetic.main.item_build_network.view.*
import kotlinx.android.synthetic.main.item_connections_recommended_group.view.*
import java.io.Serializable
import java.util.TreeSet
import kotlin.Comparator
import kotlin.collections.ArrayList

/**
 * Created by Peter on 3/27/2018.
 */
class AccountsToRecommendRVAdapter(private val bestMatchesAccountIds: List<String>) : BasePaginationRVAdapter<GroupedAccount>(), View.OnClickListener {

    private val selectedAccountsInternal: MutableSet<ShortAccountModel> = TreeSet(Comparator { first, second -> first.id.compareTo(second.id) })
    var onSelectedAccountsChangedListener = { selectedAccountIds: List<ShortAccountModel> -> }
    var selectedAccounts: Set<ShortAccountModel>
        get() = selectedAccountsInternal
        set(value) {
            if (value.size != selectedAccountsInternal.size || !selectedAccountsInternal.containsAll(value)) {
                selectedAccountsInternal.clear()
                selectedAccountsInternal.addAll(value)
                onSelectedAccountsChangedListener(selectedAccountsInternal.toList())

                notifyDataSetChanged()
            }
        }

    override var filterPredicate: (item: GroupedAccount) -> Boolean = {
        if (it is GroupedAccount.Recommendation) {
            it.account.formattedName.toLowerCase().contains(searchPhrase.toLowerCase())
        } else {
            true
        }
    }

    init {
        dataStorage = FilteredSortedDataStorage(filterPredicate, SimpleDataProviderImpl())
        searchListener = dataStorage as SearchListener<GroupedAccount>
    }

    fun searchByName(searchText: String) {
        searchPhrase = searchText
        searchListener.search()
    }

    fun destroyCallbacks() {
        onSelectedAccountsChangedListener = {}
    }

    fun setAccounts(accounts: List<ShortAccountModel>) {
        val result = ArrayList<GroupedAccount>()

        val bestMatchesAccountsList = bestMatchesAccountIds.mapNotNull { id -> accounts.firstOrNull { it.id == id } }
        if (bestMatchesAccountsList.isNotEmpty()) {
            result += GroupedAccount.Group(fromDictionary(R.string.posts_recommend_best_matches))
            bestMatchesAccountsList.mapTo(result) { GroupedAccount.Recommendation(it) }
        }

        val otherAccounts = accounts.filter { it.id !in bestMatchesAccountIds }
        if (otherAccounts.isNotEmpty()) {
            result += GroupedAccount.Group(fromDictionary(R.string.posts_recommend_other_connections))
            otherAccounts.mapTo(result) { GroupedAccount.Recommendation(it) }
        }

        set(result)
    }

    override fun onClick(view: View) {
        val viewHolder = view.tag as? AccountViewHolder
                ?: return
        val position = viewHolder.adapterPosition

        if (position >= 0) {
            val item = getDataItemByAdapterPosition(position) as GroupedAccount.Recommendation

            if (selectedAccounts.contains(item.account)) {
                selectedAccountsInternal.remove(item.account)
            } else {
                if (selectedAccountsInternal.size < MAX_SELECTED_ITEMS_COUNT) {
                    selectedAccountsInternal.add(item.account)
                }
            }
            viewHolder.bind(item)

            onSelectedAccountsChangedListener(selectedAccountsInternal.toList())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<GroupedAccount> =
            when (viewType) {
                TYPE_ITEM -> AccountViewHolder.newInstance(parent, this, selectedAccounts)
                TYPE_GROUP -> GroupViewHolder.newInstance(parent)
                else -> throw IllegalArgumentException("Illegal view type $viewType")
            }

    override fun getViewType(position: Int): Int = when (dataStorage.get(position)) {
        is GroupedAccount.Group -> TYPE_GROUP
        is GroupedAccount.Recommendation -> TYPE_ITEM
    }

    private class GroupViewHolder(itemView: View) : BaseVH<GroupedAccount>(itemView) {
        override fun bind(item: GroupedAccount) {
            itemView.tvGroupName.text = (item as GroupedAccount.Group).name
        }

        companion object {
            fun newInstance(parent: ViewGroup): GroupViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_recommended_group, parent, false)
                return GroupViewHolder(view)
            }
        }
    }

    private class AccountViewHolder(private val selectedAccount: Set<ShortAccountModel>, itemView: View) : BaseVH<GroupedAccount>(itemView) {
        override fun bind(item: GroupedAccount) {
            val account = (item as GroupedAccount.Recommendation).account

            with(itemView) {
                ivAvatar.avatarRound(account.avatar)
                tvUserName.text = account.formattedName
                tvPosition.text = account.formattedPosition
                tvPosition.goneIfEmpty()
                tvEventName.text = account.formattedFromEvent
                tvEventName.goneIfEmpty()
                cbInvite.isChecked = selectedAccount.contains(account)
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener, selectedAccount: Set<ShortAccountModel>): AccountViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_build_network, parent, false)

                val viewHolder = AccountViewHolder(selectedAccount, view)
                view.tag = viewHolder
                view.cbInvite.tag = viewHolder

                view.setOnClickListener(onClickListener)
                view.cbInvite.setOnClickListener(onClickListener)

                return viewHolder
            }
        }
    }

    private companion object {
        const val TYPE_GROUP = 1
        const val TYPE_ITEM = 2
        const val MAX_SELECTED_ITEMS_COUNT = 5
    }
}

sealed class GroupedAccount: Serializable {
    data class Group(val name: String) : GroupedAccount()
    data class Recommendation(val account: ShortAccountModel) : GroupedAccount()
}
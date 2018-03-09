package com.mnassa.screen.invite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.mainAbility
import com.mnassa.extensions.avatarRound
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_invite_account.view.*

/**
 * Created by Peter on 3/5/2018.
 */
class InviteAdapter : BasePaginationRVAdapter<ShortAccountModel>(), View.OnClickListener {

    private val selectedAccountsInternal: MutableSet<String> = HashSet()
    var onSelectedAccountsChangedListener = { selectedAccountIds: Set<String> -> }
    var selectedAccounts: Set<String>
        get() = selectedAccountsInternal
        set(value) {
            selectedAccountsInternal.clear()
            selectedAccountsInternal.addAll(value)

            val selectedAccountsCopy = selectedAccountsInternal.toList()
            val allAccountsIds = dataStorage.map { it.id }
            selectedAccountsInternal.clear()
            selectedAccountsInternal.addAll(selectedAccountsCopy.filter { allAccountsIds.contains(it) })
            onSelectedAccountsChangedListener(selectedAccountsInternal)

            notifyDataSetChanged()
        }

    override fun createView(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ShortAccountModel> {
        val view = inflater.inflate(R.layout.item_invite_account, parent, false)

        val viewHolder = InviteViewHolder(selectedAccountsInternal, view)
        view.tag = viewHolder
        view.cbInvite.tag = viewHolder

        view.setOnClickListener(this)
        view.cbInvite.setOnClickListener(this)
        return viewHolder
    }

    override fun onClick(v: View) {
        val viewHolder = v.tag as? InviteViewHolder ?: return
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
                tvUserPosition.text = item.mainAbility(fromDictionary(R.string.invite_at_placeholder))
                tvUserPosition.visibility = if (tvUserPosition.text.isNullOrBlank()) View.GONE else View.VISIBLE
                cbInvite.isChecked = selectedAccount.contains(item.id)
            }
        }
    }
}
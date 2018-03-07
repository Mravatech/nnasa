package com.mnassa.screen.invite

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.mainAbility
import com.mnassa.extensions.avatar
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_invite_account.view.*
import kotlinx.android.synthetic.main.item_invite_skip.view.*

/**
 * Created by Peter on 3/5/2018.
 */
class InviteAdapter : RecyclerView.Adapter<InviteAdapter.BaseViewHolder>(), View.OnClickListener {

    private val data: MutableList<ShortAccountModel> = ArrayList()
    private val selectedAccountsInternal: MutableSet<String> = HashSet()
    var onSelectedAccountsChangedListener = { selectedAccountIds: Set<String> -> }
    var onSkipClickListener = {}
    var selectedAccounts: Set<String>
        get() = selectedAccountsInternal
        set(value) {
            selectedAccountsInternal.clear()
            selectedAccountsInternal.addAll(value)

            val selectedAccountsCopy = selectedAccountsInternal.toList()
            val allAccountsIds = data.map { it.id }
            selectedAccountsInternal.clear()
            selectedAccountsInternal.addAll(selectedAccountsCopy.filter { allAccountsIds.contains(it) })
            onSelectedAccountsChangedListener(selectedAccountsInternal)

            notifyDataSetChanged()
        }


    fun setAccounts(newData: List<ShortAccountModel>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = data[oldItemPosition].id == newData[newItemPosition].id
            override fun getOldListSize(): Int = data.size
            override fun getNewListSize(): Int = newData.size
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = data[oldItemPosition] == newData[newItemPosition]
        }, true)
        data.clear()
        data.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int = if (position < data.size) TYPE_ITEM else TYPE_FOOTER

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_invite_account, parent, false)

                val viewHolder = InviteViewHolder(selectedAccountsInternal, view)
                view.tag = viewHolder
                view.cbInvite.tag = viewHolder

                view.setOnClickListener(this)
                view.cbInvite.setOnClickListener(this)
                viewHolder
            }
            TYPE_FOOTER -> {
                val view = inflater.inflate(R.layout.item_invite_skip, parent, false)
                view.btnSkipStep.text = fromDictionary(R.string.invite_skip_step)
                view.btnSkipStep.setOnClickListener {
                    onSkipClickListener()
                }

                FooterViewHolder(view)
            }
            else -> throw IllegalArgumentException("viewType $viewType not allowed")
        }
    }

    override fun onClick(v: View) {
        val viewHolder = v.tag as? InviteViewHolder ?: return
        val position = viewHolder.adapterPosition

        if (position >= 0) {
            val item = data[position]

            if (selectedAccounts.contains(item.id)) {
                selectedAccountsInternal.remove(item.id)
            } else selectedAccountsInternal.add(item.id)
            viewHolder.bind(item)

            onSelectedAccountsChangedListener(selectedAccountsInternal)
        }
    }

    override fun getItemCount(): Int = data.size + 1

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        (holder as? InviteViewHolder)?.bind(data[position])
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class InviteViewHolder(private val selectedAccount: Set<String>, itemView: View) : BaseViewHolder(itemView) {
        fun bind(data: ShortAccountModel) {
            with(itemView) {
                ivAvatar.avatar(data.avatar)
                tvUserName.text = data.formattedName
                tvUserPosition.text = data.mainAbility(fromDictionary(R.string.invite_at_placeholder))
                tvUserPosition.visibility = if (tvUserPosition.text.isNullOrBlank()) View.GONE else View.VISIBLE
                cbInvite.isChecked = selectedAccount.contains(data.id)
            }
        }
    }

    private class FooterViewHolder(itemView: View) : BaseViewHolder(itemView)

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
    }
}
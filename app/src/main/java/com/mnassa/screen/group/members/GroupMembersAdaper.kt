package com.mnassa.screen.group.members

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_connections_all.view.*

/**
 * Created by Peter on 5/25/2018.
 */
class GroupMembersAdaper : BasePaginationRVAdapter<GroupMemberItem>(), View.OnClickListener {
    var onItemOptionsClickListener = { account: GroupMemberItem, sender: View -> }
    var onItemClickListener = { account: GroupMemberItem -> }
    var onAfterSearchListener = { }
    override var filterPredicate: (item: GroupMemberItem) -> Boolean = { it.user.formattedName.toLowerCase().contains(searchPhrase.toLowerCase()) }

    init {
        onDataChangedListener = { onAfterSearchListener() }
        dataStorage = FilteredSortedDataStorage(filterPredicate, SimpleDataProviderImpl())
        searchListener = dataStorage as SearchListener<GroupMemberItem>
        itemsTheSameComparator = { first, second -> first.user.id == second.user.id }
        contentTheSameComparator = { first, second -> first.isAdmin == second.isAdmin }
    }

    fun searchByName(searchText: String) {
        searchPhrase = searchText
        searchListener.search()
    }

    fun destroyCallbacks() {
        onItemOptionsClickListener = { _, _ -> }
        onItemClickListener = { }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<GroupMemberItem> =
            UserViewHolder.newInstance(parent, this)

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnMoreOptions -> {
                val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onItemOptionsClickListener(getDataItemByAdapterPosition(position), view)
                }
            }
            R.id.rvConnectionRoot -> {
                val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
                if (position >= 0) {
                    onItemClickListener(getDataItemByAdapterPosition(position))
                }
            }
        }
    }


    private class UserViewHolder(itemView: View, private val clickListener: View.OnClickListener) : BaseVH<GroupMemberItem>(itemView) {

        override fun bind(item: GroupMemberItem) {
            val user = item.user
            with(itemView) {
                ivAvatar.avatarRound(user.avatar)
                tvUserName.text = user.formattedName

                tvPosition.text = user.formattedPosition
                tvPosition.goneIfEmpty()

                tvEventName.text = fromDictionary(R.string.group_role_admin).takeIf { item.isAdmin }
                tvEventName.setTextColor(Color.BLACK)
                tvEventName.goneIfEmpty()

                btnMoreOptions.setOnClickListener(clickListener)
                btnMoreOptions.tag = this@UserViewHolder

                rvConnectionRoot.setOnClickListener(clickListener)
                rvConnectionRoot.tag = this@UserViewHolder
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, clickListener: View.OnClickListener): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connections_all, parent, false)
                return UserViewHolder(view, clickListener)
            }
        }
    }
}
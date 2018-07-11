package com.mnassa.screen.profile

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.*
import com.mnassa.extensions.isGone
import com.mnassa.extensions.isMyProfile
import com.mnassa.screen.posts.PostsRVAdapter
import com.mnassa.screen.posts.viewholder.UnsupportedTypeViewHolder
import com.mnassa.screen.profile.common.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

class ProfileAdapter : PostsRVAdapter(), View.OnClickListener {

    var onConnectionsClickListener = {}
    var onWalletClickListener = {}
    var onConnectionStatusClickListener = { item: ConnectionStatus? -> }

    var profileModel: ProfileAccountModel? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }
    var offers: List<TagModel> = emptyList()
        set(value) {
            field = value
            notifyItemChanged(0)
        }
    var interests: List<TagModel> = emptyList()
        set(value) {
            field = value
            notifyItemChanged(0)
        }
    var connectionStatus: ConnectionStatus? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        onDataChangedListener = { notifyItemChanged(0) }
    }


    override fun onClick(view: View) {
        val tag = view.tag
        if (tag is GroupModel) {
            onGroupClickListener(tag)
            return
        }

        val position = (tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.tvProfileConnections -> onConnectionsClickListener()
            R.id.tvPointsGiven -> onWalletClickListener()
            R.id.tvConnectionStatus -> connectionStatus?.let(onConnectionStatusClickListener)
            R.id.rlClickableRoot -> onItemClickListener(getDataItemByAdapterPosition(position))
            R.id.flCreateNeed -> onCreateNeedClickListener()
            R.id.rlRepostRoot -> onRepostedByClickListener(requireNotNull(getDataItemByAdapterPosition(position).repostAuthor))
            R.id.rlAuthorRoot -> onPostedByClickListener(getDataItemByAdapterPosition(position).author)
            R.id.btnHidePost -> onHideInfoPostClickListener(getDataItemByAdapterPosition(position))
            R.id.btnMoreOptions -> onMoreItemClickListener(getDataItemByAdapterPosition(position), view)
        }
    }

    override fun onBindViewHolder(holder: BaseVH<PostModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is BaseProfileHolder) {
            profileModel?.let { holder.bindProfile(it) }
            holder.bindOffers(offers)
            holder.bindInterests(interests)
            connectionStatus?.let { holder.bindConnectionStatus(it) }
            holder.itemView.findViewById<View>(R.id.rlEmptyView).isGone = !dataStorage.isEmpty()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<PostModel> {
        val profile = profileModel
        return if (viewType == TYPE_HEADER) {
            val item = if (profile == null) {
                UnsupportedTypeViewHolder.newInstance(parent, this)
            } else when {
                profile.isMyProfile && profile.accountType == AccountType.ORGANIZATION -> CompanyProfileViewHolder.newInstance(parent, this, profile)
                profile.isMyProfile && profile.accountType == AccountType.PERSONAL -> PersonalProfileViewHolder.newInstance(parent, this, profile)
                !profile.isMyProfile && profile.accountType == AccountType.ORGANIZATION -> AnotherCompanyProfileHolder.newInstance(parent, this, profile)
                !profile.isMyProfile && profile.accountType == AccountType.PERSONAL -> AnotherPersonalProfileHolder.newInstance(parent, this, profile)
                else -> throw IllegalArgumentException("Wrong account type!")
            }
            item.setIsRecyclable(false)
            item
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }
}
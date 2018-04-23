package com.mnassa.screen.profile

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.posts.PostsRVAdapter
import com.mnassa.screen.profile.common.AnotherCompanyProfileHolder
import com.mnassa.screen.profile.common.AnotherPersonalProfileHolder
import com.mnassa.screen.profile.common.CompanyProfileViewHolder
import com.mnassa.screen.profile.common.PersonalProfileViewHolder
import com.mnassa.screen.profile.model.Accounts
import com.mnassa.screen.profile.model.ProfileModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

class ProfileAdapter() : PostsRVAdapter() , View.OnClickListener {

    var onConnectionsClickListener =  {}
    var onWalletClickListener =  {}
    var onConnectionStatusClickListener = { item: ConnectionStatus? -> }

    var profileModel: ProfileModel? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.tvProfileConnections -> onConnectionsClickListener()
            R.id.tvPointsGiven -> onWalletClickListener()
            R.id.tvConnectionStatus -> onConnectionStatusClickListener(requireNotNull(profileModel).connectionStatus)
            R.id.rlClickableRoot -> onItemClickListener(getDataItemByAdapterPosition(position))
            R.id.flCreateNeed -> onCreateNeedClickListener()
            R.id.rlRepostRoot -> onRepostedByClickListener(requireNotNull(getDataItemByAdapterPosition(position).repostAuthor))
            R.id.rlAuthorRoot -> onPostedByClickListener(getDataItemByAdapterPosition(position).author)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<PostModel> {
        return if (viewType == TYPE_HEADER && profileModel != null) {
            return when (requireNotNull(profileModel).getAccountType()) {
                Accounts.MY_COMPANY -> CompanyProfileViewHolder.newInstance(parent, this, requireNotNull(profileModel))
                Accounts.MY_PERSONAL -> PersonalProfileViewHolder.newInstance(parent, this, requireNotNull(profileModel))
                Accounts.USER_COMPANY -> AnotherCompanyProfileHolder.newInstance(parent, this, requireNotNull(profileModel))
                Accounts.USER_PERSONAL -> AnotherPersonalProfileHolder.newInstance(parent, this, requireNotNull(profileModel))
            }
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }
}
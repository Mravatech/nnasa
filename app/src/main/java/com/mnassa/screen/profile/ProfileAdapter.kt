package com.mnassa.screen.profile

import android.view.ViewGroup
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

class ProfileAdapter() : PostsRVAdapter() {

    var profileModel: ProfileModel? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }
    lateinit var viewModel: ProfileViewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<PostModel> {
        return if (viewType == TYPE_HEADER && profileModel != null) {
            return when (requireNotNull(profileModel).getAccountType()) {
                Accounts.MY_COMPANY -> CompanyProfileViewHolder.newInstance(parent, viewModel, requireNotNull(profileModel))
                Accounts.MY_PERSONAL -> PersonalProfileViewHolder.newInstance(parent, viewModel, requireNotNull(profileModel))
                Accounts.USER_COMPANY -> AnotherCompanyProfileHolder.newInstance(parent, viewModel, requireNotNull(profileModel))
                Accounts.USER_PERSONAL -> AnotherPersonalProfileHolder.newInstance(parent, viewModel, requireNotNull(profileModel))
            }
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }
}
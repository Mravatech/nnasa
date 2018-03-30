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

class ProfileAdapter(private val profileModel: ProfileModel, private val viewModel: ProfileViewModel) : PostsRVAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<PostModel> {
        return if (viewType == TYPE_HEADER) {
            return when (profileModel.getAccountType()) {
                Accounts.MY_COMPANY -> CompanyProfileViewHolder.newInstance(parent, viewModel, profileModel)
                Accounts.MY_PERSONAL -> PersonalProfileViewHolder.newInstance(parent, viewModel, profileModel)
                Accounts.USER_COMPANY -> AnotherCompanyProfileHolder.newInstance(parent, viewModel, profileModel)
                Accounts.USER_PERSONAL -> AnotherPersonalProfileHolder.newInstance(parent, viewModel, profileModel)
            }
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }
}
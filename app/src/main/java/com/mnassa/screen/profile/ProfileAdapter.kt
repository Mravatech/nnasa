package com.mnassa.screen.profile

import android.view.ViewGroup
import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.posts.PostsRVAdapter
import com.mnassa.screen.profile.common.AnotherCompanyProfileHolder
import com.mnassa.screen.profile.common.AnotherPersonalProfileHolder
import com.mnassa.screen.profile.common.CompanyProfileViewHolder
import com.mnassa.screen.profile.common.PersonalProfileViewHolder
import com.mnassa.screen.profile.model.ProfileModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

class ProfileAdapter(private val profileModel: ProfileModel, private val viewModel: ProfileViewModel) : PostsRVAdapter() {


    //    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<ProfileModel> {
//        return if (viewType == TYPE_HEADER) {
//            return if (profileModel.isMyProfile) {
//                when (profileModel.profile.accountType) {
//                    AccountType.PERSONAL -> PersonalProfileViewHolder.newInstance(parent, viewModel)
//                    AccountType.ORGANIZATION -> CompanyProfileViewHolder.newInstance(parent, viewModel)
//                }
//            } else {
//                when (profileModel.profile.accountType) {
//                    AccountType.PERSONAL -> AnotherPersonalProfileHolder.newInstance(parent)
//                    AccountType.ORGANIZATION -> AnotherCompanyProfileHolder.newInstance(parent)
//                }
//            }
//        } else {
//            super.onCreateViewHolder(parent, viewType)
//        }
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<PostModel> {
        return if (viewType == TYPE_HEADER) {
            return if (profileModel.isMyProfile) {
                when (profileModel.profile.accountType) {
                    AccountType.PERSONAL -> PersonalProfileViewHolder.newInstance(parent, viewModel, profileModel)
                    AccountType.ORGANIZATION -> CompanyProfileViewHolder.newInstance(parent, viewModel, profileModel)
                }
            } else {
                when (profileModel.profile.accountType) {
                    AccountType.PERSONAL -> AnotherPersonalProfileHolder.newInstance(parent, profileModel)
                    AccountType.ORGANIZATION -> AnotherCompanyProfileHolder.newInstance(parent, profileModel)
                }
            }
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ProfileModel> {
//        return if (profileModel.isMyProfile) {
//            when (profileModel.profile.accountType) {
//                AccountType.PERSONAL -> PersonalProfileViewHolder.newInstance(parent, viewModel)
//                AccountType.ORGANIZATION -> CompanyProfileViewHolder.newInstance(parent, viewModel)
//            }
//        } else {
//            when (profileModel.profile.accountType) {
//                AccountType.PERSONAL -> AnotherPersonalProfileHolder.newInstance(parent)
//                AccountType.ORGANIZATION -> AnotherCompanyProfileHolder.newInstance(parent)
//            }
//        }
//    }


}
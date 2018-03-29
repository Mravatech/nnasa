package com.mnassa.screen.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mnassa.domain.model.AccountType
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
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

class ProfileAdapter(private val profileModel: ProfileModel, private val viewModel: ProfileViewModel) : BasePaginationRVAdapter<ProfileModel>() {

    private val selectedAccountsInternal: MutableList<ProfileModel> = mutableListOf()
    var data: List<ProfileModel>
        get() = selectedAccountsInternal
        set(value) {
            selectedAccountsInternal.clear()
            selectedAccountsInternal.addAll(value)

            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ProfileModel> {
        return if (profileModel.isMyProfile) {
            when (profileModel.profile.accountType) {
                AccountType.PERSONAL -> PersonalProfileViewHolder.newInstance(parent, viewModel)
                AccountType.ORGANIZATION -> CompanyProfileViewHolder.newInstance(parent, viewModel)
            }
        } else {
            when (profileModel.profile.accountType) {
                AccountType.PERSONAL -> AnotherPersonalProfileHolder.newInstance(parent)
                AccountType.ORGANIZATION -> AnotherCompanyProfileHolder.newInstance(parent)
            }
        }
    }


}
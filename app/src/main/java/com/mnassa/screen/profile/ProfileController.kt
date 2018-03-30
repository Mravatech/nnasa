package com.mnassa.screen.profile

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.allconnections.AllConnectionsController
import com.mnassa.screen.profile.edit.company.EditCompanyProfileController
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileController
import com.mnassa.screen.profile.model.ProfileModel
import kotlinx.android.synthetic.main.controller_profile.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class ProfileController(data: Bundle) : MnassaControllerImpl<ProfileViewModel>(data) {

    override val layoutId: Int = R.layout.controller_profile
    override val viewModel: ProfileViewModel by instance()
    private val accountModel: ShortAccountModel? by lazy { args.getSerializable(EXTRA_ACCOUNT) as ShortAccountModel? }
    private val accountId: String by lazy { args.getString(EXTRA_ACCOUNT_ID) }

    private lateinit var adapter: ProfileAdapter
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.ivProfileBack.setOnClickListener { close() }
        accountModel?.let {
            viewModel.getProfileWithAccountId(it.id)
            view.ivCropImage.avatarSquare(it.avatar)
        } ?: run {
            viewModel.getProfileWithAccountId(accountId)
        }
        view.appBarLayout.addOnOffsetChangedListener({ appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                view.tvTitleCollapsed.visibility = View.VISIBLE
            } else {
                view.tvTitleCollapsed.visibility = View.GONE
            }
        })
        launchCoroutineUI {
            viewModel.profileChannel.consumeEach { profileModel ->
                adapter = ProfileAdapter(profileModel, viewModel)
                view.rvProfile.layoutManager = LinearLayoutManager(view.context)
                view.rvProfile.adapter = adapter
                adapter.set(listOf(profileModel))
                view.ivCropImage.avatarSquare(profileModel.profile.avatar)
                setTitle(profileModel, view)
                if (profileModel.isMyProfile) {
                    view.ivProfileEdit.visibility = View.VISIBLE
                    view.ivProfileEdit.setOnClickListener {
                        open(when (profileModel.profile.accountType) {
                            AccountType.PERSONAL -> EditPersonalProfileController.newInstance(profileModel)
                            AccountType.ORGANIZATION -> EditCompanyProfileController.newInstance(profileModel)
                        })
                    }
                } else {
                    view.ivProfileMenu.visibility = View.VISIBLE
                    view.fabProfile.visibility = View.VISIBLE
                    view.ivProfileMenu.setOnClickListener { }
                    view.fabProfile.setOnClickListener { }
                }
            }
        }
        launchCoroutineUI {
            viewModel.profileClickChannel.consumeEach {
                when (it) {
                    is ProfileViewModel.ProfileCommand.ProfileConnection -> open(AllConnectionsController.newInstance())
                    is ProfileViewModel.ProfileCommand.ProfileWallet -> Toast.makeText(view.context, "ProfileWallet", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setTitle(profileModel: ProfileModel, view: View) {
        if (profileModel.profile.accountType == AccountType.PERSONAL) {
            view.profileName.text = "${profileModel.profile.personalInfo?.firstName} ${profileModel.profile.personalInfo?.lastName}"
            if (profileModel.profile.abilities.isNotEmpty()) {
                view.profileSubName.text = profileModel.profile.abilities[0].place
            }
            view.tvTitleCollapsed.text = "${profileModel.profile.personalInfo?.firstName} ${profileModel.profile.personalInfo?.lastName}"
        } else {
            view.profileName.text = profileModel.profile.organizationInfo?.organizationName
            view.profileSubName.text = profileModel.profile.organizationType
            view.tvTitleCollapsed.text = profileModel.profile.organizationInfo?.organizationName
        }
    }

    companion object {
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
        fun newInstance(account: ShortAccountModel): ProfileController {
            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNT, account)
            return ProfileController(params)
        }

        fun newInstance(accountId: String): ProfileController {
            val params = Bundle()
            params.putString(EXTRA_ACCOUNT_ID, accountId)
            return ProfileController(params)
        }
    }
}
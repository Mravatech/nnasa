package com.mnassa.screen.profile

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.AccountType
import com.mnassa.extensions.avatarSquare
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.profile.edit.company.EditCompanyProfileController
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileController
import kotlinx.android.synthetic.main.controller_profile.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class ProfileController : MnassaControllerImpl<ProfileViewModel>() {

    override val layoutId: Int = R.layout.controller_profile
    override val viewModel: ProfileViewModel by instance()

    private lateinit var adapter: ProfileAdapter
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.ivProfileBack.setOnClickListener { close() }
//        open(AllConnectionsController.newInstance())
        viewModel.getProfileWithAccountId("-L8h7TSSSi1KL0KOcee9") //TODO set account my
//        viewModel.getProfileWithAccountId("-L7iL1VRfulD0PIQBT7V") //TODO set account id serega
//        viewModel.getProfileWithAccountId("-L7ixl179JFvjPnaZxXn") //TODO set account id lena
        launchCoroutineUI {
            viewModel.profileChannel.consumeEach { profileModel ->
                view.rvProfile.layoutManager = LinearLayoutManager(view.context)
                adapter = ProfileAdapter(profileModel)
                view.rvProfile.adapter = adapter
                adapter.set(listOf(profileModel))
                view.ivCropImage.avatarSquare(profileModel.profile.avatar)
                view.toolbarProfile.title = "${profileModel.profile.personalInfo?.firstName} ${profileModel.profile.personalInfo?.lastName}"
                view.toolbarProfile.subtitle = "${profileModel.profile.personalInfo?.firstName} ${profileModel.profile.personalInfo?.lastName}"
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
    }

    companion object {
        fun newInstance() = ProfileController()
    }
}
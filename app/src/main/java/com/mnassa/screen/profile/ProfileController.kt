package com.mnassa.screen.profile

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.avatarSquare
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.profile.edit.EditProfileController
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

    var adapter = ProfileAdapter()
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.ivProfileBack.setOnClickListener { close() }
//        open(AllConnectionsController.newInstance())
        viewModel.getProfileWithAccountId("-L7iL1VRfulD0PIQBT7V") //TODO set account id
        launchCoroutineUI {
            viewModel.profileChannel.consumeEach { profileModel ->
                view.rvProfile.layoutManager = LinearLayoutManager(view.context)
                view.rvProfile.adapter = adapter
                adapter.set(listOf(profileModel))
                view.ivCropImage.avatarSquare(profileModel.profile.avatar)
                view.toolbarProfile.title = "${profileModel.profile.personalInfo?.firstName} ${profileModel.profile.personalInfo?.lastName}"
                view.toolbarProfile.subtitle = "${profileModel.profile.personalInfo?.firstName} ${profileModel.profile.personalInfo?.lastName}"
                if (profileModel.isMyProfile) {
                    view.ivProfileEdit.visibility = View.VISIBLE
                    view.ivProfileEdit.setOnClickListener { open(EditProfileController.newInstance(profileModel.profile)) }
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
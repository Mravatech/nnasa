package com.mnassa.screen.accountinfo.organization

import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 2/28/2018.
 */
class OrganizationInfoController : MnassaControllerImpl<OrganizationInfoViewModel>() {
    override val layoutId: Int = R.layout.controller_organization_info
    override val viewModel: OrganizationInfoViewModel by instance()

    companion object {
        fun newInstance() = OrganizationInfoController()
    }
}
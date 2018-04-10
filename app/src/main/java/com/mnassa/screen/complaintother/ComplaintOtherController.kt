package com.mnassa.screen.complaintother

import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */
class ComplaintOtherController: MnassaControllerImpl<ComplaintOtherViewModel>() {
    override val layoutId: Int = R.layout.controller_complaint_other
    override val viewModel: ComplaintOtherViewModel by instance()

    companion object {
        fun newInstance() = ComplaintOtherController()
    }

}
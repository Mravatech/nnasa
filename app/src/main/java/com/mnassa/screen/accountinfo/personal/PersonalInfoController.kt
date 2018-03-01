package com.mnassa.screen.accountinfo.personal

import android.os.Bundle
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_personal_info.view.*

/**
 * Created by Peter on 2/27/2018.
 */
class PersonalInfoController(data: Bundle) : MnassaControllerImpl<PersonalInfoViewModel>(data) {
    override val layoutId: Int = R.layout.controller_personal_info
    override val viewModel: PersonalInfoViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        view.btnNext.setOnClickListener {



        }
    }

    companion object {

        fun newInstance(): PersonalInfoController {
            val params = Bundle()
            return PersonalInfoController(params)
        }
    }
}
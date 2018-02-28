package com.mnassa.screen.registration.second

import android.os.Bundle
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.registration.FirstRegistrationStepData

/**
 * Created by Peter on 2/27/2018.
 */
class PersonalInfoController(data: Bundle) : MnassaControllerImpl<PersonalInfoViewModel>(data) {
    override val layoutId: Int = R.layout.controller_personal_info
    override val viewModel: PersonalInfoViewModel by instance()

    companion object {
        private const val EXTRA_FIRST_STEP_DATA = "EXTRA_FIRST_STEP_DATA"

        fun newInstance(firstStepData: FirstRegistrationStepData): PersonalInfoController {
            val params = Bundle()
            params.putSerializable(EXTRA_FIRST_STEP_DATA, firstStepData)
            return PersonalInfoController(params)
        }
    }
}
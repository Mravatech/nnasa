package com.mnassa.screen.registration.first

import android.os.Bundle
import com.github.salomonbrys.kodein.instance
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.data.network.api.FirebaseAuthApi
import com.mnassa.data.network.bean.retrofit.RegisterOrganizationAccountRequets
import com.mnassa.data.network.bean.retrofit.RegisterPersonalAccountRequest
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import timber.log.Timber

/**
 * Created by Peter on 2/26/2018.
 */
class RegistrationViewModelImpl(private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), RegistrationViewModel {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val x: FirebaseAuthApi by instance<FirebaseAuthApi>()

        launchCoroutineUI {

            try {
//                x.registerPersonalAccount(RegisterPersonalAccountRequest(
//                        "firstName",
//                        "lastName",
//                        "UserName1",
//                        "personal",
//                        "My offers",
//                        "Interests"
//                )).await()

                x.registerOrganizationAccount(RegisterOrganizationAccountRequets(
                        "UserName112", "organization", "OrgName!!!", "off1", "int1")).await()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }


    }

}
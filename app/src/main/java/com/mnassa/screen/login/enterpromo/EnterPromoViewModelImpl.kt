package com.mnassa.screen.login.enterpromo

import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.screen.login.enterphone.EnterPhoneViewModelImpl

/**
 * Created by Peter on 3/1/2018.
 */
class EnterPromoViewModelImpl(loginInteractor: LoginInteractor, userProfileInteractor: UserProfileInteractor)
    : EnterPhoneViewModelImpl(loginInteractor, userProfileInteractor), EnterPromoViewModel
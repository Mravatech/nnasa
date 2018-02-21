package com.mnassa.di

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextImpl
import com.androidkotlincore.entityconverter.registerConverter
import com.github.salomonbrys.kodein.*
import com.mnassa.data.converter.UserProfileConverter
import com.mnassa.data.repository.UserRepositoryImpl
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.impl.LoginInteractorImpl
import com.mnassa.domain.interactor.impl.UserProfileInteractorImpl
import com.mnassa.domain.repository.UserRepository
import com.mnassa.screen.login.LoginViewModel
import com.mnassa.screen.login.LoginViewModelImpl
import com.mnassa.screen.main.MainViewModel
import com.mnassa.screen.main.MainViewModelImpl
import com.mnassa.screen.splash.SplashViewModel
import com.mnassa.screen.splash.SplashViewModelImpl

/**
 * Created by Peter on 2/20/2018.
 */
val viewModelsModule = Kodein.Module {
    bind<SplashViewModel>() with provider { SplashViewModelImpl(instance()) }
    bind<LoginViewModel>() with provider { LoginViewModelImpl() }
    bind<MainViewModel>() with provider { MainViewModelImpl() }
}

val convertersModule = Kodein.Module {
    bind<ConvertersContext>() with singleton {
        val converter = ConvertersContextImpl()
        converter.registerConverter(UserProfileConverter::class.java)
        converter
    }
}

val repositoryModule = Kodein.Module {
    bind<UserRepository>() with singleton { UserRepositoryImpl(instance(), instance()) }
}

val interactorModule = Kodein.Module {
    bind<UserProfileInteractor>() with singleton { UserProfileInteractorImpl(instance()) }
    bind<LoginInteractor>() with singleton { LoginInteractorImpl(instance()) }
}
package com.mnassa.di

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.mnassa.screen.splash.SplashViewModel
import com.mnassa.screen.splash.SplashViewModelImpl

/**
 * Created by Peter on 2/20/2018.
 */
val viewModelsModule = Kodein.Module {
    bind<SplashViewModel>() with provider {
        SplashViewModelImpl()
    }
}
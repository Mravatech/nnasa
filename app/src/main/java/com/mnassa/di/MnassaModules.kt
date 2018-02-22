package com.mnassa.di

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextImpl
import com.androidkotlincore.entityconverter.registerConverter
import com.github.salomonbrys.kodein.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mnassa.data.converter.TagConverter
import com.mnassa.data.converter.UserProfileConverter
import com.mnassa.data.network.networkModule
import com.mnassa.data.repository.TagRepositoryImpl
import com.mnassa.data.repository.UserRepositoryImpl
import com.mnassa.data.service.LoginServiceImpl
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.impl.LoginInteractorImpl
import com.mnassa.domain.interactor.impl.UserProfileInteractorImpl
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import com.mnassa.domain.service.LoginService
import com.mnassa.other.AppInfoProviderImpl
import com.mnassa.screen.login.LoginViewModel
import com.mnassa.screen.login.LoginViewModelImpl
import com.mnassa.screen.main.MainViewModel
import com.mnassa.screen.main.MainViewModelImpl
import com.mnassa.screen.splash.SplashViewModel
import com.mnassa.screen.splash.SplashViewModelImpl

/**
 * Created by Peter on 2/20/2018.
 */

fun registerAppModules(kodeinBuilder: Kodein.Builder) {
    kodeinBuilder.apply {
        import(viewModelsModule)
        import(convertersModule)
        import(repositoryModule)
        import(serviceModule)
        import(interactorModule)
        import(otherModule)
        import(networkModule)
    }
}

private val viewModelsModule = Kodein.Module {
    bind<SplashViewModel>() with provider { SplashViewModelImpl(instance()) }
    bind<LoginViewModel>() with provider { LoginViewModelImpl(instance()) }
    bind<MainViewModel>() with provider { MainViewModelImpl(instance(), instance(), instance()) }
}

private val convertersModule = Kodein.Module {
    bind<ConvertersContext>() with singleton {
        val converter = ConvertersContextImpl()
        converter.registerConverter(UserProfileConverter::class.java)
        converter.registerConverter(TagConverter::class.java)
        converter
    }
}

private val repositoryModule = Kodein.Module {
    bind<FirebaseDatabase>() with provider {
        val result = FirebaseDatabase.getInstance()
        result.setPersistenceEnabled(true)
        result
    }
    bind<DatabaseReference>() with provider { instance<FirebaseDatabase>().reference }
    bind<UserRepository>() with singleton { UserRepositoryImpl(instance(), instance(), instance(), instance()) }
    bind<TagRepository>() with singleton { TagRepositoryImpl(instance()) }
}

private val serviceModule = Kodein.Module {
    bind<LoginService>() with singleton { LoginServiceImpl() }
}

private val interactorModule = Kodein.Module {
    bind<UserProfileInteractor>() with singleton { UserProfileInteractorImpl(instance()) }
    bind<LoginInteractor>() with singleton { LoginInteractorImpl(instance(), instance()) }
}

private val otherModule = Kodein.Module {
    bind<AppInfoProvider>() with singleton { AppInfoProviderImpl(instance()) }
}
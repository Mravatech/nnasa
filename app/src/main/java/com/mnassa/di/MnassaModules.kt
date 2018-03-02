package com.mnassa.di

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextImpl
import com.androidkotlincore.entityconverter.registerConverter
import com.github.salomonbrys.kodein.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.mnassa.data.converter.TagConverter
import com.mnassa.data.converter.TranslatedWordConverter
import com.mnassa.data.converter.UserAccountConverter
import com.mnassa.data.network.RetrofitConfig
import com.mnassa.data.network.api.FirebaseAuthApi
import com.mnassa.data.network.api.FirebaseDictionaryApi
import com.mnassa.data.network.exception.FirebaseExceptionHandler
import com.mnassa.data.network.exception.FirebaseExceptionHandlerImpl
import com.mnassa.data.network.exception.NetworkExceptionHandlerImpl
import com.mnassa.data.network.exception.NetworkExceptionHandler
import com.mnassa.data.repository.DictionaryRepositoryImpl
import com.mnassa.data.repository.TagRepositoryImpl
import com.mnassa.data.repository.UserRepositoryImpl
import com.mnassa.data.service.FirebaseLoginServiceImpl
import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.impl.DictionaryInteractorImpl
import com.mnassa.domain.interactor.impl.LoginInteractorImpl
import com.mnassa.domain.interactor.impl.UserProfileInteractorImpl
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.domain.repository.DictionaryRepository
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import com.mnassa.domain.service.FirebaseLoginService
import com.mnassa.AppInfoProviderImpl
import com.mnassa.translation.LanguageProviderImpl
import com.mnassa.screen.accountinfo.organization.OrganizationInfoViewModel
import com.mnassa.screen.accountinfo.organization.OrganizationInfoViewModelImpl
import com.mnassa.screen.login.entercode.EnterCodeViewModel
import com.mnassa.screen.login.entercode.EnterCodeViewModelImpl
import com.mnassa.screen.login.enterphone.EnterPhoneViewModel
import com.mnassa.screen.login.enterphone.EnterPhoneViewModelImpl
import com.mnassa.screen.login.selectaccount.SelectAccountViewModel
import com.mnassa.screen.login.selectaccount.SelectAccountViewModelIImpl
import com.mnassa.screen.main.MainViewModel
import com.mnassa.screen.main.MainViewModelImpl
import com.mnassa.screen.registration.RegistrationViewModel
import com.mnassa.screen.registration.RegistrationViewModelImpl
import com.mnassa.screen.accountinfo.personal.PersonalInfoViewModel
import com.mnassa.screen.accountinfo.personal.PersonalInfoViewModelImpl
import com.mnassa.screen.login.enterpromo.EnterPromoViewModel
import com.mnassa.screen.login.enterpromo.EnterPromoViewModelImpl
import com.mnassa.screen.splash.SplashViewModel
import com.mnassa.screen.splash.SplashViewModelImpl
import retrofit2.Retrofit

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
    bind<EnterPhoneViewModel>() with provider { EnterPhoneViewModelImpl(instance()) }
    bind<MainViewModel>() with provider { MainViewModelImpl(instance(), instance()) }
    bind<EnterCodeViewModel>() with provider { EnterCodeViewModelImpl(instance()) }
    bind<RegistrationViewModel>() with provider { RegistrationViewModelImpl(instance()) }
    bind<PersonalInfoViewModel>() with provider { PersonalInfoViewModelImpl() }
    bind<SelectAccountViewModel>() with provider { SelectAccountViewModelIImpl(instance()) }
    bind<OrganizationInfoViewModel>() with provider { OrganizationInfoViewModelImpl() }
    bind<EnterPromoViewModel>() with provider { EnterPromoViewModelImpl(instance()) }
}

private val convertersModule = Kodein.Module {
    bind<ConvertersContext>() with singleton {
        val converter = ConvertersContextImpl()
        converter.registerConverter(UserAccountConverter::class.java)
        converter.registerConverter(TagConverter::class.java)
        converter.registerConverter(TranslatedWordConverter::class.java)
        converter
    }
}

private val repositoryModule = Kodein.Module {
    bind<FirebaseDatabase>() with singleton {
        val result = FirebaseDatabase.getInstance()
        result.setPersistenceEnabled(true)
        result
    }
    bind<DatabaseReference>() with provider { instance<FirebaseDatabase>().reference }
    bind<UserRepository>() with singleton { UserRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
    bind<TagRepository>() with singleton { TagRepositoryImpl(instance(), instance()) }
    bind<DictionaryRepository>() with singleton { DictionaryRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
}

private val serviceModule = Kodein.Module {
    bind<FirebaseLoginService>() with singleton { FirebaseLoginServiceImpl(instance(), instance(), instance()) }
}

private val interactorModule = Kodein.Module {
    bind<UserProfileInteractor>() with singleton { UserProfileInteractorImpl(instance()) }
    bind<LoginInteractor>() with singleton { LoginInteractorImpl(instance(), instance()) }
    bind<DictionaryInteractor>() with singleton { DictionaryInteractorImpl(instance()) }
}

private val networkModule = Kodein.Module {
    bind<Gson>() with singleton { Gson() }
    bind<RetrofitConfig>() with singleton { RetrofitConfig({ instance() }, { instance() },  { instance() } , { instance() }) }
    bind<Retrofit>() with singleton {
        instance<RetrofitConfig>().makeRetrofit()
    }
    bind<FirebaseAuthApi>() with singleton {
        val retrofit: Retrofit = instance()
        retrofit.create(FirebaseAuthApi::class.java)
    }
    bind<FirebaseDictionaryApi>() with singleton {
        val retrofit: Retrofit = instance()
        retrofit.create(FirebaseDictionaryApi::class.java)
    }
    bind<NetworkExceptionHandler>() with singleton { NetworkExceptionHandlerImpl(instance(), instance()) }
    bind<FirebaseExceptionHandler>() with singleton { FirebaseExceptionHandlerImpl() }
}

private val otherModule = Kodein.Module {
    bind<AppInfoProvider>() with singleton { AppInfoProviderImpl(instance()) }
    bind<LanguageProvider>() with singleton { LanguageProviderImpl(instance()) }
}
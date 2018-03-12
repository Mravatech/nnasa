package com.mnassa.di

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextImpl
import com.androidkotlincore.entityconverter.registerConverter
import com.github.salomonbrys.kodein.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
import com.mnassa.data.repository.StorageRepositoryImpl
import com.mnassa.data.repository.TagRepositoryImpl
import com.mnassa.data.repository.UserRepositoryImpl
import com.mnassa.data.service.FirebaseLoginServiceImpl
import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.impl.DictionaryInteractorImpl
import com.mnassa.domain.interactor.impl.LoginInteractorImpl
import com.mnassa.domain.interactor.impl.StorageInteractorImpl
import com.mnassa.domain.interactor.impl.UserProfileInteractorImpl
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.domain.repository.DictionaryRepository
import com.mnassa.domain.repository.StorageRepository
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository
import com.mnassa.domain.service.FirebaseLoginService
import com.mnassa.screen.profile.ProfileViewModel
import com.mnassa.screen.profile.ProfileViewModelImpl
import com.mnassa.AppInfoProviderImpl
import com.mnassa.data.converter.ConnectionsConverter
import com.mnassa.data.network.api.FirebaseInviteApi
import com.mnassa.data.network.exception.*
import com.mnassa.data.repository.*
import com.mnassa.domain.interactor.*
import com.mnassa.domain.interactor.impl.*
import com.mnassa.domain.repository.*
import com.mnassa.dialog.DialogHelper
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
import com.mnassa.screen.chats.ChatListViewModel
import com.mnassa.screen.chats.ChatListViewModelImpl
import com.mnassa.screen.connections.ConnectionsViewModel
import com.mnassa.screen.connections.ConnectionsViewModelImpl
import com.mnassa.screen.connections.archived.ArchivedConnectionViewModel
import com.mnassa.screen.connections.archived.ArchivedConnectionViewModelImpl
import com.mnassa.screen.connections.newrequests.NewRequestsViewModel
import com.mnassa.screen.connections.newrequests.NewRequestsViewModelImpl
import com.mnassa.screen.connections.recommended.RecommendedConnectionsViewModel
import com.mnassa.screen.connections.recommended.RecommendedConnectionsViewModelImpl
import com.mnassa.screen.connections.sent.SentConnectionsViewModel
import com.mnassa.screen.connections.sent.SentConnectionsViewModelImpl
import com.mnassa.screen.events.EventsViewModel
import com.mnassa.screen.events.EventsViewModelImpl
import com.mnassa.screen.home.HomeViewModel
import com.mnassa.screen.home.HomeViewModelImpl
import com.mnassa.screen.invite.InviteViewModel
import com.mnassa.screen.invite.InviteViewModelImpl
import com.mnassa.screen.login.enterpromo.EnterPromoViewModel
import com.mnassa.screen.login.enterpromo.EnterPromoViewModelImpl
import com.mnassa.screen.needs.NeedsViewModel
import com.mnassa.screen.needs.NeedsViewModelImpl
import com.mnassa.screen.notifications.NotificationsViewModel
import com.mnassa.screen.notifications.NotificationsViewModelImpl
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
    bind<MainViewModel>() with provider { MainViewModelImpl(instance(), instance(), instance()) }
    bind<EnterCodeViewModel>() with provider { EnterCodeViewModelImpl(instance()) }
    bind<RegistrationViewModel>() with provider { RegistrationViewModelImpl(instance()) }
    bind<SelectAccountViewModel>() with provider { SelectAccountViewModelIImpl(instance(), instance()) }
    bind<OrganizationInfoViewModel>() with provider { OrganizationInfoViewModelImpl() }
    bind<EnterPromoViewModel>() with provider { EnterPromoViewModelImpl(instance()) }
    bind<PersonalInfoViewModel>() with provider { PersonalInfoViewModelImpl(instance(), instance(), instance()) }
    bind<ProfileViewModel>() with provider { ProfileViewModelImpl(instance(), instance()) }
    bind<InviteViewModel>() with provider { InviteViewModelImpl(instance()) }
    bind<HomeViewModel>() with provider { HomeViewModelImpl(instance()) }
    bind<NeedsViewModel>() with provider { NeedsViewModelImpl() }
    bind<EventsViewModel>() with provider { EventsViewModelImpl() }
    bind<ConnectionsViewModel>() with provider { ConnectionsViewModelImpl(instance()) }
    bind<NotificationsViewModel>() with provider { NotificationsViewModelImpl() }
    bind<ChatListViewModel>() with provider { ChatListViewModelImpl() }
    bind<RecommendedConnectionsViewModel>() with provider { RecommendedConnectionsViewModelImpl(instance()) }
    bind<NewRequestsViewModel>() with provider { NewRequestsViewModelImpl(instance()) }
    bind<SentConnectionsViewModel>() with provider { SentConnectionsViewModelImpl(instance()) }
    bind<ArchivedConnectionViewModel>() with provider { ArchivedConnectionViewModelImpl(instance()) }
}

private val convertersModule = Kodein.Module {
    bind<ConvertersContext>() with singleton {
        val converter = ConvertersContextImpl()
        converter.registerConverter(UserAccountConverter::class.java)
        converter.registerConverter(TagConverter::class.java)
        converter.registerConverter(TranslatedWordConverter::class.java)
        converter.registerConverter(ConnectionsConverter::class.java)
        converter
    }
}

private val repositoryModule = Kodein.Module {
    bind<FirebaseDatabase>() with singleton {
        val result = FirebaseDatabase.getInstance()
        result.setPersistenceEnabled(true)
        result
    }
    bind<FirebaseStorage>() with singleton { FirebaseStorage.getInstance() }
    bind<DatabaseReference>() with provider { instance<FirebaseDatabase>().reference }
    bind<StorageReference>() with provider { instance<FirebaseStorage>().reference }
    bind<UserRepository>() with singleton { UserRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
    bind<TagRepository>() with singleton { TagRepositoryImpl(instance(), instance()) }
    bind<DictionaryRepository>() with singleton { DictionaryRepositoryImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bind<ConnectionsRepository>() with singleton { ConnectionsRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
    bind<ContactsRepository>() with singleton { PhoneContactRepositoryImpl(instance(), instance()) }
    bind<StorageRepository>() with singleton { StorageRepositoryImpl(instance(), instance()) }
    bind<CountersRepository>() with singleton { CountersRepositoryImpl(instance(), instance(), instance()) }
}

private val serviceModule = Kodein.Module {
    bind<FirebaseLoginService>() with singleton { FirebaseLoginServiceImpl(instance(), instance(), instance()) }
}

private val interactorModule = Kodein.Module {
    bind<UserProfileInteractor>() with singleton { UserProfileInteractorImpl(instance()) }
    bind<LoginInteractor>() with singleton { LoginInteractorImpl(instance(), instance()) }
    bind<DictionaryInteractor>() with singleton { DictionaryInteractorImpl(instance()) }
    bind<ConnectionsInteractor>() with singleton { ConnectionsInteractorImpl(instance(), instance()) }
    bind<StorageInteractor>() with singleton { StorageInteractorImpl(instance(), instance()) }
    bind<CountersInteractor>() with singleton { CountersInteractorImpl(instance()) }
}

private val networkModule = Kodein.Module {
    bind<Gson>() with singleton { Gson() }
    bind<RetrofitConfig>() with singleton { RetrofitConfig({ instance() }, { instance() },  { instance() } , { instance() }, { instance() }) }
    bind<Retrofit>() with singleton {
        instance<RetrofitConfig>().makeRetrofit()
    }

    //firebase functions API
    bind<FirebaseAuthApi>() with singleton {
        val retrofit: Retrofit = instance()
        retrofit.create(FirebaseAuthApi::class.java)
    }
    bind<FirebaseDictionaryApi>() with singleton {
        val retrofit: Retrofit = instance()
        retrofit.create(FirebaseDictionaryApi::class.java)
    }
    bind<FirebaseInviteApi>() with singleton {
        val retrofit: Retrofit = instance()
        retrofit.create(FirebaseInviteApi::class.java)
    }


    //exception handlers
    bind<NetworkExceptionHandler>() with singleton { NetworkExceptionHandlerImpl(instance(), instance()) }
    bind<FirebaseExceptionHandler>() with singleton { FirebaseExceptionHandlerImpl() }
    bind<ExceptionHandler>() with singleton { ExceptionHandlerImpl( { instance() }, { instance() }) }
}

private val otherModule = Kodein.Module {
    bind<AppInfoProvider>() with singleton { AppInfoProviderImpl(instance()) }
    bind<LanguageProvider>() with singleton { LanguageProviderImpl(instance()) }
    bind<DialogHelper>() with singleton { DialogHelper() }
}
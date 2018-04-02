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
import com.mnassa.AppInfoProviderImpl
import com.mnassa.helper.CountryHelper
import com.mnassa.data.converter.*
import com.mnassa.data.network.RetrofitConfig
import com.mnassa.data.network.api.*
import com.mnassa.data.network.exception.handler.*
import com.mnassa.data.repository.*
import com.mnassa.data.service.FirebaseLoginServiceImpl
import com.mnassa.helper.DialogHelper
import com.mnassa.domain.interactor.*
import com.mnassa.domain.interactor.impl.*
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.domain.repository.*
import com.mnassa.domain.service.FirebaseLoginService
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.helper.IntentHelper
import com.mnassa.screen.accountinfo.organization.OrganizationInfoViewModel
import com.mnassa.screen.accountinfo.organization.OrganizationInfoViewModelImpl
import com.mnassa.screen.accountinfo.personal.PersonalInfoViewModel
import com.mnassa.screen.accountinfo.personal.PersonalInfoViewModelImpl
import com.mnassa.screen.buildnetwork.BuildNetworkViewModel
import com.mnassa.screen.buildnetwork.BuildNetworkViewModelImpl
import com.mnassa.screen.chats.ChatListViewModel
import com.mnassa.screen.chats.ChatListViewModelImpl
import com.mnassa.screen.connections.ConnectionsViewModel
import com.mnassa.screen.connections.ConnectionsViewModelImpl
import com.mnassa.screen.connections.allconnections.AllConnectionsViewModel
import com.mnassa.screen.connections.allconnections.AllConnectionsViewModelImpl
import com.mnassa.screen.connections.archived.ArchivedConnectionViewModel
import com.mnassa.screen.connections.archived.ArchivedConnectionViewModelImpl
import com.mnassa.screen.connections.newrequests.NewRequestsViewModel
import com.mnassa.screen.connections.newrequests.NewRequestsViewModelImpl
import com.mnassa.screen.connections.recommended.RecommendedConnectionsViewModel
import com.mnassa.screen.connections.recommended.RecommendedConnectionsViewModelImpl
import com.mnassa.screen.connections.select.SelectConnectionViewModel
import com.mnassa.screen.connections.select.SelectConnectionViewModelImpl
import com.mnassa.screen.connections.sent.SentConnectionsViewModel
import com.mnassa.screen.connections.sent.SentConnectionsViewModelImpl
import com.mnassa.screen.events.EventsViewModel
import com.mnassa.screen.events.EventsViewModelImpl
import com.mnassa.screen.home.HomeViewModel
import com.mnassa.screen.home.HomeViewModelImpl
import com.mnassa.screen.invite.InviteViewModel
import com.mnassa.screen.invite.InviteViewModelImpl
import com.mnassa.screen.invite.history.HistoryViewModel
import com.mnassa.screen.invite.history.HistoryViewModelImpl
import com.mnassa.screen.login.entercode.EnterCodeViewModel
import com.mnassa.screen.login.entercode.EnterCodeViewModelImpl
import com.mnassa.screen.login.enterphone.EnterPhoneViewModel
import com.mnassa.screen.login.enterphone.EnterPhoneViewModelImpl
import com.mnassa.screen.login.enterpromo.EnterPromoViewModel
import com.mnassa.screen.login.enterpromo.EnterPromoViewModelImpl
import com.mnassa.screen.login.selectaccount.SelectAccountViewModel
import com.mnassa.screen.login.selectaccount.SelectAccountViewModelIImpl
import com.mnassa.screen.main.MainViewModel
import com.mnassa.screen.main.MainViewModelImpl
import com.mnassa.screen.notifications.NotificationsViewModel
import com.mnassa.screen.notifications.NotificationsViewModelImpl
import com.mnassa.screen.posts.PostsViewModel
import com.mnassa.screen.posts.PostsViewModelImpl
import com.mnassa.screen.posts.need.create.CreateNeedViewModel
import com.mnassa.screen.posts.need.create.CreateNeedViewModelImpl
import com.mnassa.screen.posts.need.details.PostDetailsViewModel
import com.mnassa.screen.posts.need.details.PostDetailsViewModelImpl
import com.mnassa.screen.posts.need.recommend.RecommendViewModel
import com.mnassa.screen.posts.need.recommend.RecommendViewModelImpl
import com.mnassa.screen.posts.need.sharing.SharingOptionsViewModel
import com.mnassa.screen.posts.need.sharing.SharingOptionsViewModelImpl
import com.mnassa.screen.profile.ProfileViewModel
import com.mnassa.screen.profile.ProfileViewModelImpl
import com.mnassa.screen.registration.RegistrationViewModel
import com.mnassa.screen.registration.RegistrationViewModelImpl
import com.mnassa.screen.splash.SplashViewModel
import com.mnassa.screen.splash.SplashViewModelImpl
import com.mnassa.screen.wallet.WalletViewModel
import com.mnassa.screen.wallet.WalletViewModelImpl
import com.mnassa.screen.wallet.send.SendPointsViewModel
import com.mnassa.screen.wallet.send.SendPointsViewModelImpl
import com.mnassa.translation.LanguageProviderImpl
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
    bind<RegistrationViewModel>() with provider { RegistrationViewModelImpl(instance(), instance(), instance()) }
    bind<SelectAccountViewModel>() with provider { SelectAccountViewModelIImpl(instance(), instance()) }
    bind<OrganizationInfoViewModel>() with provider { OrganizationInfoViewModelImpl() }
    bind<EnterPromoViewModel>() with provider { EnterPromoViewModelImpl(instance()) }
    bind<PersonalInfoViewModel>() with provider { PersonalInfoViewModelImpl(instance(), instance(), instance()) }
    bind<ProfileViewModel>() with provider { ProfileViewModelImpl(instance(), instance()) }
    bind<BuildNetworkViewModel>() with provider { BuildNetworkViewModelImpl(instance()) }
    bind<HomeViewModel>() with provider { HomeViewModelImpl(instance()) }
    bind<PostsViewModel>() with provider { PostsViewModelImpl(instance()) }
    bind<EventsViewModel>() with provider { EventsViewModelImpl() }
    bind<ConnectionsViewModel>() with provider { ConnectionsViewModelImpl(instance()) }
    bind<NotificationsViewModel>() with provider { NotificationsViewModelImpl() }
    bind<ChatListViewModel>() with provider { ChatListViewModelImpl() }
    bind<RecommendedConnectionsViewModel>() with provider { RecommendedConnectionsViewModelImpl(instance()) }
    bind<NewRequestsViewModel>() with provider { NewRequestsViewModelImpl(instance()) }
    bind<SentConnectionsViewModel>() with provider { SentConnectionsViewModelImpl(instance()) }
    bind<ArchivedConnectionViewModel>() with provider { ArchivedConnectionViewModelImpl(instance()) }
    bind<AllConnectionsViewModel>() with provider { AllConnectionsViewModelImpl(instance()) }
    bind<CreateNeedViewModel>() with factory { postId: String? -> CreateNeedViewModelImpl(postId, instance(), instance(), instance(), instance()) }
    bind<PostDetailsViewModel>() with factory { postId: String -> PostDetailsViewModelImpl(postId, instance(), instance(), instance()) }
    bind<InviteViewModel>() with provider { InviteViewModelImpl(instance(), instance(), instance()) }
    bind<HistoryViewModel>() with provider { HistoryViewModelImpl(instance()) }
    bind<SharingOptionsViewModel>() with provider { SharingOptionsViewModelImpl(instance()) }
    bind<RecommendViewModel>() with provider { RecommendViewModelImpl(instance()) }
    bind<WalletViewModel>() with provider { WalletViewModelImpl(instance()) }
    bind<SendPointsViewModel>() with provider { SendPointsViewModelImpl(instance()) }
    bind<SelectConnectionViewModel>() with provider { SelectConnectionViewModelImpl(instance()) }
}

private val convertersModule = Kodein.Module {
    bind<ConvertersContext>() with singleton {
        val converter = ConvertersContextImpl()
        converter.registerConverter(UserAccountConverter::class.java)
        converter.registerConverter(TranslatedWordConverter(instance()))
        converter.registerConverter(ConnectionsConverter::class.java)
        converter.registerConverter(GeoPlaceConverter::class.java)
        converter.registerConverter(TagConverter(instance()))
        converter.registerConverter(LocationConverter(instance()))
        converter.registerConverter(PostConverter::class.java)
        converter.registerConverter(CommentsConverter::class.java)
        converter.registerConverter(WalletConverter( { instance() } ))
        converter.registerConverter(InvitationConverter::class.java)
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
    bind<TagRepository>() with singleton { TagRepositoryImpl(instance(), instance(), instance(), instance()) }
    bind<DictionaryRepository>() with singleton { DictionaryRepositoryImpl(instance(), { instance() }, instance(), instance(), instance(), instance(), instance()) }
    bind<StorageRepository>() with singleton { StorageRepositoryImpl(instance(), instance()) }
    bind<ConnectionsRepository>() with singleton { ConnectionsRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
    bind<ContactsRepository>() with singleton { PhoneContactRepositoryImpl(instance(), instance()) }
    bind<CountersRepository>() with singleton { CountersRepositoryImpl(instance(), instance(), instance()) }
    bind<PlaceFinderRepository>() with singleton { PlaceFinderRepositoryImpl(instance<PlayServiceHelper>().googleApiClient, instance()) }
    bind<InviteRepository>() with singleton { InviteRepositoryImpl(instance(), instance(), instance(), instance()) }
    bind<PostsRepository>() with singleton { PostsRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
    bind<CommentsRepository>() with singleton { CommentsRepositoryImpl(instance(), instance(), exceptionHandler = instance(COMMENTS_EXCEPTION_HANDLER)) }
    bind<WalletRepository>() with singleton { WalletRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
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
    bind<TagInteractor>() with singleton { TagInteractorImpl(instance()) }
    bind<CountersInteractor>() with singleton { CountersInteractorImpl(instance()) }
    bind<PlaceFinderInteractor>() with singleton { PlaceFinderInteractorImpl(instance()) }
    bind<PostsInteractor>() with singleton { PostsInteractorImpl(instance(), instance(), instance()) }
    bind<CommentsInteractor>() with singleton { CommentsInteractorImpl(instance()) }
    bind<WalletInteractor>() with singleton { WalletInteractorImpl(instance()) }
    bind<InviteInteractor>() with singleton { InviteInteractorImpl(instance(), instance()) }
}

private const val COMMENTS_EXCEPTION_HANDLER = "COMMENTS_EXCEPTION_HANDLER"

private val networkModule = Kodein.Module {
    bind<Gson>() with singleton { Gson() }
    bind<RetrofitConfig>() with singleton { RetrofitConfig({ instance() }, { instance() }, { instance() }, { instance() }, { instance() }) }
    bind<Retrofit>() with singleton { instance<RetrofitConfig>().makeRetrofit() }

    //firebase functions API
    bindRetrofitApi<FirebaseAuthApi>()
    bindRetrofitApi<FirebaseDictionaryApi>()
    bindRetrofitApi<FirebaseInviteApi>()
    bindRetrofitApi<FirebaseTagsApi>()
    bindRetrofitApi<FirebasePostApi>()
    bindRetrofitApi<FirebaseCommentsApi>()
    bindRetrofitApi<FirebaseWalletApi>()

    //exception handlers
    bind<NetworkExceptionHandler>() with singleton { NetworkExceptionHandlerImpl(instance(), instance()) }
    bind<NetworkExceptionHandler>(COMMENTS_EXCEPTION_HANDLER) with singleton { CommentsExceptionHandler(instance(), instance()) }
    bind<FirebaseExceptionHandler>() with singleton { FirebaseExceptionHandlerImpl() }
    bind<ExceptionHandler>() with singleton { ExceptionHandlerImpl({ instance() }, { instance() }) }
    bind<ExceptionHandler>(COMMENTS_EXCEPTION_HANDLER) with singleton { ExceptionHandlerImpl({ instance() }, { instance(COMMENTS_EXCEPTION_HANDLER) }) }
}

private inline fun <reified T : Any> Kodein.Builder.bindRetrofitApi() {
    bind<T>() with singleton {
        val retrofit: Retrofit = instance()
        retrofit.create(T::class.java)
    }
}

private val otherModule = Kodein.Module {
    bind<AppInfoProvider>() with singleton { AppInfoProviderImpl(instance()) }
    bind<LanguageProvider>() with singleton { LanguageProviderImpl() }
    bind<DialogHelper>() with singleton { DialogHelper() }
    bind<PopupMenuHelper>() with singleton { PopupMenuHelper(instance()) }
    bind<IntentHelper>() with singleton { IntentHelper() }
    bind<CountryHelper>() with singleton { CountryHelper(instance()) }
    bind<PlayServiceHelper>() with singleton { PlayServiceHelper(instance()) }
}
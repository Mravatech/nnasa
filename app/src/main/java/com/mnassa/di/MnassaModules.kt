package com.mnassa.di

import android.os.Bundle
import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextImpl
import com.androidkotlincore.entityconverter.registerConverter
import com.bluelinelabs.conductor.Controller
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.mnassa.AppInfoProviderImpl
import com.mnassa.data.converter.*
import com.mnassa.data.network.RetrofitConfig
import com.mnassa.data.network.api.*
import com.mnassa.data.network.exception.handler.*
import com.mnassa.data.repository.*
import com.mnassa.data.service.FirebaseLoginServiceImpl
import com.mnassa.domain.interactor.*
import com.mnassa.domain.interactor.impl.*
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.domain.repository.*
import com.mnassa.domain.service.FirebaseLoginService
import com.mnassa.helper.*
import com.mnassa.screen.accountinfo.organization.OrganizationInfoViewModel
import com.mnassa.screen.accountinfo.organization.OrganizationInfoViewModelImpl
import com.mnassa.screen.accountinfo.personal.PersonalInfoViewModel
import com.mnassa.screen.accountinfo.personal.PersonalInfoViewModelImpl
import com.mnassa.screen.buildnetwork.BuildNetworkViewModel
import com.mnassa.screen.buildnetwork.BuildNetworkViewModelImpl
import com.mnassa.screen.chats.ChatListViewModel
import com.mnassa.screen.chats.ChatListViewModelImpl
import com.mnassa.screen.chats.message.ChatMessageViewModel
import com.mnassa.screen.chats.message.ChatMessageViewModelImpl
import com.mnassa.screen.comments.CommentsWrapperForEventViewModelImpl
import com.mnassa.screen.comments.CommentsWrapperForPostViewModelImpl
import com.mnassa.screen.comments.CommentsWrapperViewModel
import com.mnassa.screen.complaintother.ComplaintOtherViewModel
import com.mnassa.screen.complaintother.ComplaintOtherViewModelImpl
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
import com.mnassa.screen.events.details.EventDetailsController
import com.mnassa.screen.events.details.EventDetailsViewModel
import com.mnassa.screen.events.details.EventDetailsViewModelImpl
import com.mnassa.screen.events.details.info.EventDetailsInfoController
import com.mnassa.screen.events.details.info.EventDetailsInfoViewModel
import com.mnassa.screen.events.details.info.EventDetailsInfoViewModelImpl
import com.mnassa.screen.events.details.participants.EventDetailsParticipantsViewModel
import com.mnassa.screen.events.details.participants.EventDetailsParticipantsViewModelImpl
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
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.posts.PostsViewModel
import com.mnassa.screen.posts.PostsViewModelImpl
import com.mnassa.screen.posts.general.details.GeneralPostController
import com.mnassa.screen.posts.general.details.GeneralPostViewModelImpl
import com.mnassa.screen.posts.need.create.CreateNeedViewModel
import com.mnassa.screen.posts.need.create.CreateNeedViewModelImpl
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.screen.posts.need.details.NeedDetailsViewModel
import com.mnassa.screen.posts.need.details.NeedDetailsViewModelImpl
import com.mnassa.screen.posts.need.recommend.RecommendViewModel
import com.mnassa.screen.posts.need.recommend.RecommendViewModelImpl
import com.mnassa.screen.posts.need.sharing.SharingOptionsViewModel
import com.mnassa.screen.posts.need.sharing.SharingOptionsViewModelImpl
import com.mnassa.screen.posts.profile.create.RecommendUserViewModel
import com.mnassa.screen.posts.profile.create.RecommendUserViewModelImpl
import com.mnassa.screen.posts.profile.details.RecommendedProfileController
import com.mnassa.screen.posts.profile.details.RecommendedProfileViewModel
import com.mnassa.screen.posts.profile.details.RecommendedProfileViewModelImpl
import com.mnassa.screen.profile.ProfileViewModel
import com.mnassa.screen.profile.ProfileViewModelImpl
import com.mnassa.screen.profile.edit.company.EditCompanyProfileViewModel
import com.mnassa.screen.profile.edit.company.EditCompanyProfileViewModelImpl
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileViewModel
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileViewModelImpl
import com.mnassa.screen.registration.RegistrationViewModel
import com.mnassa.screen.registration.RegistrationViewModelImpl
import com.mnassa.screen.splash.SplashViewModel
import com.mnassa.screen.splash.SplashViewModelImpl
import com.mnassa.screen.termsandconditions.TermsAndConditionsViewModel
import com.mnassa.screen.termsandconditions.TermsAndConditionsViewModelImpl
import com.mnassa.screen.wallet.WalletViewModel
import com.mnassa.screen.wallet.WalletViewModelImpl
import com.mnassa.screen.wallet.send.SendPointsViewModel
import com.mnassa.screen.wallet.send.SendPointsViewModelImpl
import com.mnassa.translation.LanguageProviderImpl
import org.kodein.di.Kodein
import org.kodein.di.generic.*
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
    bind<EnterPhoneViewModel>() with provider { EnterPhoneViewModelImpl(instance(), instance()) }
    bind<MainViewModel>() with provider { MainViewModelImpl(instance(), instance(), instance(), instance()) }
    bind<EnterCodeViewModel>() with provider { EnterCodeViewModelImpl(instance(), instance()) }
    bind<RegistrationViewModel>() with provider { RegistrationViewModelImpl(instance(), instance(), instance()) }
    bind<SelectAccountViewModel>() with provider { SelectAccountViewModelIImpl(instance()) }
    bind<OrganizationInfoViewModel>() with provider { OrganizationInfoViewModelImpl(instance(), instance()) }
    bind<EnterPromoViewModel>() with provider { EnterPromoViewModelImpl(instance(), instance()) }
    bind<PersonalInfoViewModel>() with provider { PersonalInfoViewModelImpl(instance(), instance()) }
    bind<ProfileViewModel>() with provider { ProfileViewModelImpl(instance(), instance(), instance(), instance(), instance()) }
    bind<BuildNetworkViewModel>() with provider { BuildNetworkViewModelImpl(instance()) }
    bind<HomeViewModel>() with provider { HomeViewModelImpl(instance()) }
    bind<PostsViewModel>() with provider { PostsViewModelImpl(instance()) }
    bind<EventsViewModel>() with provider { EventsViewModelImpl(instance()) }
    bind<ConnectionsViewModel>() with provider { ConnectionsViewModelImpl(instance()) }
    bind<NotificationsViewModel>() with provider { NotificationsViewModelImpl(instance()) }
    bind<ChatListViewModel>() with provider { ChatListViewModelImpl(instance()) }
    bind<ChatMessageViewModel>() with provider { ChatMessageViewModelImpl(instance(), instance()) }
    bind<RecommendedConnectionsViewModel>() with provider { RecommendedConnectionsViewModelImpl(instance()) }
    bind<NewRequestsViewModel>() with provider { NewRequestsViewModelImpl(instance()) }
    bind<SentConnectionsViewModel>() with provider { SentConnectionsViewModelImpl(instance()) }
    bind<ArchivedConnectionViewModel>() with provider { ArchivedConnectionViewModelImpl(instance()) }
    bind<AllConnectionsViewModel>() with provider { AllConnectionsViewModelImpl(instance()) }
    bind<CreateNeedViewModel>() with factory { postId: String? -> CreateNeedViewModelImpl(postId, instance(), instance(), instance(), instance()) }
    bind<NeedDetailsViewModel>() with factory { postId: String -> NeedDetailsViewModelImpl(postId, instance(), instance(), instance()) }
    bind<RecommendedProfileViewModel>() with factory { postId: String -> RecommendedProfileViewModelImpl(postId, instance(), instance(), instance(), instance()) }
    bind<GeneralPostViewModelImpl>() with factory { postId: String -> GeneralPostViewModelImpl(postId, instance(), instance(), instance()) }
    bind<InviteViewModel>() with provider { InviteViewModelImpl(instance(), instance()) }
    bind<HistoryViewModel>() with provider { HistoryViewModelImpl(instance()) }
    bind<SharingOptionsViewModel>() with provider { SharingOptionsViewModelImpl(instance()) }
    bind<RecommendViewModel>() with provider { RecommendViewModelImpl(instance()) }
    bind<EditPersonalProfileViewModel>() with provider { EditPersonalProfileViewModelImpl(instance(), instance(), instance(), instance()) }
    bind<EditCompanyProfileViewModel>() with provider { EditCompanyProfileViewModelImpl(instance(), instance(), instance(), instance()) }
    bind<WalletViewModel>() with provider { WalletViewModelImpl(instance()) }
    bind<SendPointsViewModel>() with provider { SendPointsViewModelImpl(instance()) }
    bind<SelectConnectionViewModel>() with provider { SelectConnectionViewModelImpl(instance()) }
    bind<RecommendUserViewModel>() with factory { postId: String? -> RecommendUserViewModelImpl(postId, instance(), instance()) }
    bind<ComplaintOtherViewModel>() with provider { ComplaintOtherViewModelImpl() }
    bind<TermsAndConditionsViewModel>() with provider { TermsAndConditionsViewModelImpl() }
    bind<CommentsWrapperViewModel>() with factory { pair: Pair<Class<Controller>, Bundle> ->
        when (pair.first) {
            NeedDetailsController::class.java,
            RecommendedProfileController::class.java,
            GeneralPostController::class.java ->
                CommentsWrapperForPostViewModelImpl(
                        postId = pair.second.getString(NeedDetailsController.EXTRA_NEED_ID),
                        commentsInteractor = instance(),
                        postsInteractor = instance()) as CommentsWrapperViewModel //android studio bug
            EventDetailsInfoController::class.java ->
                CommentsWrapperForEventViewModelImpl(
                    eventId = pair.second.getString(EventDetailsController.EXTRA_EVENT_ID),
                    commentsInteractor = instance(),
                    eventsInteractor = instance()) as CommentsWrapperViewModel //android studio bug
            else -> throw IllegalArgumentException("Controller ${pair.first} not supported for CommentsWrapper!")
        }
    }
    bind<EventDetailsViewModel>() with factory { eventId: String -> EventDetailsViewModelImpl(eventId, instance()) }
    bind<EventDetailsInfoViewModel>() with factory { eventId: String -> EventDetailsInfoViewModelImpl(eventId, instance()) }
    bind<EventDetailsParticipantsViewModel>() with factory { eventId: String -> EventDetailsParticipantsViewModelImpl(eventId, instance(), instance(), instance()) }
}

private val convertersModule = Kodein.Module {
    bind<ConvertersContext>() with singleton {
        val converter = ConvertersContextImpl()
        converter.registerConverter(UserAccountConverter())
        converter.registerConverter(TranslatedWordConverter(instance()))
        converter.registerConverter(ConnectionsConverter())
        converter.registerConverter(GeoPlaceConverter())
        converter.registerConverter(TagConverter(instance()))
        converter.registerConverter(LocationConverter(instance()))
        converter.registerConverter(ProfileConverter(instance()))
        converter.registerConverter(AbilityConverter())
        converter.registerConverter(PostConverter())
        converter.registerConverter(CommentsConverter())
        converter.registerConverter(WalletConverter({ instance() }))
        converter.registerConverter(InvitationConverter())
        converter.registerConverter(ChatConverter::class.java)
        converter.registerConverter(EventsConverter())
        converter.registerConverter(NotificationsConverter())
        converter
    }
}

private val repositoryModule = Kodein.Module {
    bind<FirebaseDatabase>() with singleton {
        val result = FirebaseDatabase.getInstance()
        result.setPersistenceEnabled(true)
        result
    }
    bind<FirebaseFirestore>() with singleton {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()
        db.firestoreSettings = settings
        db
    }
    bind<FirebaseStorage>() with singleton { FirebaseStorage.getInstance() }
    bind<DatabaseReference>() with provider { instance<FirebaseDatabase>().reference }
    bind<StorageReference>() with provider { instance<FirebaseStorage>().reference }
    bind<UserRepository>() with singleton { UserRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
    bind<TagRepository>() with singleton { TagRepositoryImpl(instance(), instance(), instance(), instance()) }
    bind<DictionaryRepository>() with singleton { DictionaryRepositoryImpl(instance(), { instance() }, instance(), instance(), instance(), instance(), instance()) }
    bind<StorageRepository>() with singleton { StorageRepositoryImpl(instance(), instance()) }
    bind<ConnectionsRepository>() with singleton { ConnectionsRepositoryImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bind<ContactsRepository>() with singleton { PhoneContactRepositoryImpl(instance(), instance()) }
    bind<CountersRepository>() with singleton { CountersRepositoryImpl(instance(), instance(), instance()) }
    bind<PlaceFinderRepository>() with singleton { PlaceFinderRepositoryImpl(instance<PlayServiceHelper>().googleApiClient, instance()) }
    bind<InviteRepository>() with singleton { InviteRepositoryImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bind<PostsRepository>() with singleton { PostsRepositoryImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bind<CommentsRepository>() with singleton { CommentsRepositoryImpl(instance(), instance(), exceptionHandler = instance(COMMENTS_EXCEPTION_HANDLER)) }
    bind<WalletRepository>() with singleton { WalletRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
    bind<ChatRepository>() with singleton { ChatRepositoryImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bind<ComplaintRepository>() with singleton { ComplaintRepositoryImpl(instance(), instance(),instance(), instance()) }
    bind<EventsRepository>() with singleton { EventsRepositoryImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bind<NotificationRepository>() with singleton { NotificationRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }
}

private val serviceModule = Kodein.Module {
    bind<FirebaseLoginService>() with singleton { FirebaseLoginServiceImpl(instance(), instance(), instance()) }
}

private val interactorModule = Kodein.Module {
    bind<UserProfileInteractor>() with singleton { UserProfileInteractorImpl(instance()) }
    bind<LoginInteractor>() with singleton { LoginInteractorImpl(instance(), instance()) }
    bind<DictionaryInteractor>() with singleton { DictionaryInteractorImpl({ instance() }) }
    bind<ConnectionsInteractor>() with singleton { ConnectionsInteractorImpl(instance(), instance()) }
    bind<StorageInteractor>() with singleton { StorageInteractorImpl(instance(), instance()) }
    bind<TagInteractor>() with singleton { TagInteractorImpl(instance()) }
    bind<CountersInteractor>() with singleton { CountersInteractorImpl(instance()) }
    bind<PlaceFinderInteractor>() with singleton { PlaceFinderInteractorImpl(instance()) }
    bind<PostsInteractor>() with singleton { PostsInteractorImpl(instance(), instance(), instance(), instance()) }
    bind<CommentsInteractor>() with singleton { CommentsInteractorImpl(instance()) }
    bind<WalletInteractor>() with singleton { WalletInteractorImpl(instance()) }
    bind<InviteInteractor>() with singleton { InviteInteractorImpl(instance(), instance()) }
    bind<EventsInteractor>() with singleton { EventsInteractorImpl(instance(), instance()) }
    bind<ChatInteractor>() with singleton { ChatInteractorImpl(instance(), instance()) }
    bind<ComplaintInteractor>() with singleton { ComplaintInteractorImpl(instance()) }
    bind<NotificationInteractor>() with singleton { NotificationInteractorImpl(instance()) }
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
    bindRetrofitApi<FirebaseConnectionsApi>()
    bindRetrofitApi<FirebaseWalletApi>()
    bindRetrofitApi<FirebaseChatApi>()
    bindRetrofitApi<FirebaseComplaintApi>()
    bindRetrofitApi<FirebaseEventsApi>()
    bindRetrofitApi<FirebaseNotificationsApi>()

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
    bind<PostDetailsFactory>() with singleton { PostDetailsFactory() }
}
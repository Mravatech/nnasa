package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.iid.FirebaseInstanceId
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.extensions.toListChannel
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseAuthApi
import com.mnassa.data.network.bean.firebase.InviteShortAccountDbEntity
import com.mnassa.data.network.bean.firebase.ProfileDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.request.*
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.repository.DatabaseContract.TABLE_PUBLIC_ACCOUNTS
import com.mnassa.domain.model.*
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch

/**
 * Created by Peter on 2/21/2018.
 */
class UserRepositoryImpl(
        private val converter: ConvertersContext,
        private val context: Context,
        private val exceptionHandler: ExceptionHandler,
        private val db: DatabaseReference,
        private val firebaseAuthApi: FirebaseAuthApi) : UserRepository {

    private val sharedPrefs by lazy { context.getSharedPreferences(EXTRA_PREFS_NAME, Context.MODE_PRIVATE) }
    private var accountIdInternal: String?
        get() {
            return if (FirebaseAuth.getInstance().currentUser?.uid != null) {
                sharedPrefs.getString(EXTRA_ACCOUNT_ID, null)
            } else {
                sharedPrefs.edit().remove(EXTRA_ACCOUNT_ID).apply()
                null
            }
        }
        set(value) = sharedPrefs.edit().putString(EXTRA_ACCOUNT_ID, value).apply()

    private val innerCurrentProfileChannel = ConflatedBroadcastChannel<ShortAccountModel>()
    override val currentProfile: BroadcastChannel<ShortAccountModel>
        get() {
            if (innerCurrentProfileChannel.valueOrNull == null) {
                launch {
                    getCurrentAccount()?.let { innerCurrentProfileChannel.send(it) }
                }
            }

            //TODO: update user when model was changed

            return innerCurrentProfileChannel
        }

    override suspend fun getAllAccounts(): ReceiveChannel<List<ShortAccountModel>> {
        val uid = getFirebaseUserId() ?: return produce { }
        return db.child(DatabaseContract.TABLE_ACCOUNT_LINKS).child(uid)
                .toListChannel<ShortAccountDbEntity>(exceptionHandler)
                .map { converter.convertCollection(it, ShortAccountModel::class.java) }
    }

    override suspend fun setCurrentAccount(account: ShortAccountModel?) {
        if (account == null) {
            //clear account
            accountIdInternal = null
        } else {
            require(!account.id.isBlank())
            this.accountIdInternal = account.id
            this.innerCurrentProfileChannel.send(account)
            addPushToken()
        }
    }

    override suspend fun getCurrentAccount(): ShortAccountModel? {
        val accountId = accountIdInternal ?: return null

        val bean = db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(accountId)
                .await<ShortAccountDbEntity>(exceptionHandler) ?: return null
        return converter.convert(bean)
    }

    override suspend fun getCurrentAccountChannel(): ReceiveChannel<InvitedShortAccountModel> {
        val accountId = getAccountId() ?: return produce { }
        return db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(accountId)
                .toValueChannel<InviteShortAccountDbEntity>(exceptionHandler).map {
                    converter.convert<InvitedShortAccountModel>(requireNotNull(it))
                }
    }

    override suspend fun getAccounts(): List<ShortAccountModel> {
        val localFirebaseUserId = getFirebaseUserId() ?: return emptyList()
        val beans = db.child(DatabaseContract.TABLE_ACCOUNT_LINKS).child(localFirebaseUserId).awaitList<ShortAccountDbEntity>(exceptionHandler)
        return converter.convertCollection(beans, ShortAccountModel::class.java)
    }

    override suspend fun createPersonAccount(firstName: String, secondName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel {
        val result = firebaseAuthApi.registerPersonalAccount(RegisterPersonalAccountRequest(
                firstName = firstName,
                lastName = secondName,
                userName = userName,
                type = NetworkContract.AccountType.PERSONAL,
                offers = offers,
                interests = interests,
                location = Location(city),
                locationId = city
        )).handleException(exceptionHandler)
        return converter.convert(result.account)
    }

    override suspend fun createOrganizationAccount(companyName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel {
        val result = firebaseAuthApi.registerOrganizationAccount(RegisterOrganizationAccountRequest(
                userName = userName,
                type = NetworkContract.AccountType.ORGANIZATION,
                offers = offers,
                interests = interests,
                organizationName = companyName,
                location = Location(city),
                locationId = city
        )).handleException(exceptionHandler)
        return converter.convert(result.account)
    }

    override suspend fun processAccount(account: PersonalInfoModel) {
        firebaseAuthApi.registerSendAccountInfo(RegisterSendingAccountInfoRequest(
                birthdayDate = account.birthdayDate,
                lastName = account.personalInfo?.lastName,
                userName = account.userName,
                showContactEmail = account.showContactEmail,
                language = account.language,
                type = getAccountType(account.accountType),
                birthday = account.birthday,
                contactPhone = account.contactPhone,
                abilities = converter.convertCollection(account.abilities, Ability::class.java),
                id = requireNotNull(getAccountId()),
                avatar = account.avatar,
                firstName = account.personalInfo?.firstName,
                showContactPhone = account.showContactPhone,
                contactEmail = account.contactEmail,
                gender = getGender(account.gender)
        )).handleException(exceptionHandler)
    }

    override suspend fun updatePersonalAccount(account: ProfilePersonalInfoModel) {
        firebaseAuthApi.profileUpdatePersonAccountInfo(ProfilePersonAccountInfoRequest(
                birthdayDate = account.birthdayDate,
                lastName = account.personalInfo?.lastName,
                userName = account.userName,
                showContactEmail = account.showContactEmail,
                language = account.language,
                type = getAccountType(account.accountType),
                birthday = account.birthday,
                contactPhone = account.contactPhone,
                abilities = converter.convertCollection(account.abilities, Ability::class.java),
                id = requireNotNull(getAccountId()),
                avatar = account.avatar,
                firstName = account.personalInfo?.firstName,
                showContactPhone = account.showContactPhone,
                contactEmail = account.contactEmail,
                gender = getGender(account.gender),
                locationId = account.locationId,
                interests = account.interests,
                offers = account.offers
        )).handleException(exceptionHandler)
    }

    override suspend fun processAccount(account: CompanyInfoModel) {
        firebaseAuthApi.registerSendCompanyAccountInfo(RegisterSendingCompanyAccountInfoRequest(
                organizationName = requireNotNull(account.organizationInfo).organizationName,
                avatar = account.avatar,
                showContactEmail = account.showContactEmail,
                contactEmail = account.contactEmail,
                userName = account.userName,
                language = account.language,
                type = getAccountType(account.accountType),
                founded = account.founded,
                id = requireNotNull(getAccountId()),
                website = account.website,
                organizationType = account.organizationType
        )).handleException(exceptionHandler)
    }

    override suspend fun updateCompanyAccount(account: ProfileCompanyInfoModel) {
        firebaseAuthApi.profileUpdateCompanyAccountInfo(ProfileCompanyAccountInfoRequest(
                organizationName = requireNotNull(account.organizationInfo).organizationName,
                avatar = account.avatar,
                showContactEmail = account.showContactEmail,
                contactEmail = account.contactEmail,
                userName = account.userName,
                language = account.language,
                type = getAccountType(account.accountType),
                founded = account.founded,
                id = requireNotNull(getAccountId()),
                website = account.website,
                organizationType = account.organizationType,
                locationId = account.locationId,
                interests = account.interests,
                offers = account.offers
        )).handleException(exceptionHandler)
    }

    override suspend fun getProfileByAccountId(accountId: String): ProfileAccountModel? {
        val dbChild = if (accountId == getAccountId()) DatabaseContract.TABLE_ACCOUNTS else DatabaseContract.TABLE_PUBLIC_ACCOUNTS
        val profile = db.child(dbChild)
                .child(accountId)
                .apply { keepSynced(true) }
                .await<ProfileDbEntity>(exceptionHandler) ?: return null
        return converter.convert(profile)
    }

    override suspend fun getProfileById(accountId: String): ReceiveChannel<ProfileAccountModel?> {
        val dbChild = if (accountId == getAccountId()) DatabaseContract.TABLE_ACCOUNTS else DatabaseContract.TABLE_PUBLIC_ACCOUNTS
        return db.child(dbChild)
                .child(accountId)
                .apply { keepSynced(true) }
                .toValueChannel<ProfileDbEntity>(exceptionHandler)
                .map { converter.convert(it!!, ProfileAccountModel::class.java) }
    }

    override suspend fun getFirebaseToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return user.getIdToken(false).await(exceptionHandler).token
    }

    override suspend fun getFirebaseUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    override fun getAccountId(): String? = accountIdInternal

    override suspend fun getAccountById(id: String): ShortAccountModel? {
        return db
                .child(TABLE_PUBLIC_ACCOUNTS)
                .child(id)
                .await<ShortAccountDbEntity>(exceptionHandler)
                ?.run { converter.convert(this) }

    }

    override suspend fun addPushToken() {//todo add recheck token
        val token = FirebaseInstanceId.getInstance().token
        if (getAccountId() != null && token != null) {
            val info = "$ANDROID,${getFirebaseUserId()},${getAccountId()}"
            firebaseAuthApi.addPushToken(PushTokenRequest(token, info)).handleException(exceptionHandler)
        }
    }

    private fun getAccountType(type: AccountType) = when (type) {
        AccountType.PERSONAL -> NetworkContract.AccountType.PERSONAL
        AccountType.ORGANIZATION -> NetworkContract.AccountType.ORGANIZATION
    }

    private fun getGender(gender: Gender) = when (gender) {
        Gender.FEMALE -> NetworkContract.Gender.FEMALE
        Gender.MALE -> NetworkContract.Gender.MALE
    }

    companion object {
        private const val ANDROID = "Android"
        private const val EXTRA_PREFS_NAME = "USER_REPOSITORY_PREFS"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
    }
}
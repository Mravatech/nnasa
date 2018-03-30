package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseAuthApi
import com.mnassa.data.network.bean.firebase.ProfileDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.request.*
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.network.exception.handleException
import com.mnassa.domain.model.*
import com.mnassa.data.network.bean.retrofit.request.RegisterSendingAccountInfoRequest
import com.mnassa.data.repository.DatabaseContract.TABLE_PUBLIC_ACCOUNTS
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.UserRepository

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


    override suspend fun setCurrentUserAccount(account: ShortAccountModel?) {
        if (account == null) {
            //clear account
            accountIdInternal = null
        } else {
            require(!account.id.isBlank())
            this.accountIdInternal = account.id
        }
    }

    override suspend fun getCurrentUser(): ShortAccountModel? {
        val accountId = accountIdInternal ?: return null

        val bean = db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(accountId)
                .await<ShortAccountDbEntity>(exceptionHandler) ?: return null
        return converter.convert(bean)
    }

    override suspend fun getAccounts(): List<ShortAccountModel> {
        val user = FirebaseAuth.getInstance().currentUser ?: return emptyList()
        val beans = db.child(DatabaseContract.TABLE_ACCOUNT_LINKS).child(user.uid).awaitList<ShortAccountDbEntity>(exceptionHandler)
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
                type = account.accountType.name.toLowerCase(),
                birthday = account.birthday,
                contactPhone = account.contactPhone,
                abilities = converter.convertCollection(account.abilities, Ability::class.java),
                id = requireNotNull(getAccountId()),
                avatar = account.avatar,
                firstName = account.personalInfo?.firstName,
                showContactPhone = account.showContactPhone,
                contactEmail = account.contactEmail,
                gender = account.gender.name.toLowerCase()
        )).handleException(exceptionHandler)
    }

    override suspend fun updatePersonalAccount(account: ProfilePersonalInfoModel) {
        firebaseAuthApi.profileUpdatePersonAccountInfo(ProfilePersonAccountInfoRequest(
                birthdayDate = account.birthdayDate,
                lastName = account.personalInfo?.lastName,
                userName = account.userName,
                showContactEmail = account.showContactEmail,
                language = account.language,
                type = account.accountType.name.toLowerCase(),
                birthday = account.birthday,
                contactPhone = account.contactPhone,
                abilities = converter.convertCollection(account.abilities, Ability::class.java),
                id = requireNotNull(getAccountId()),
                avatar = account.avatar,
                firstName = account.personalInfo?.firstName,
                showContactPhone = account.showContactPhone,
                contactEmail = account.contactEmail,
                gender = account.gender.name.toLowerCase(),
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
                type = account.accountType.name.toLowerCase(),
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
                type = account.accountType.name.toLowerCase(),
                founded = account.founded,
                id = requireNotNull(getAccountId()),
                website = account.website,
                organizationType = account.organizationType,
                locationId = account.locationId,
                interests = account.interests,
                offers = account.offers
        )).handleException(exceptionHandler)
    }

    override suspend fun getPrifileByAccountId(accountId: String): ProfileAccountModel? {
        val dbChild = if (accountId == getAccountId()) DatabaseContract.TABLE_ACCOUNTS else DatabaseContract.TABLE_PUBLIC_ACCOUNTS
        val profile = db.child(dbChild)
                .child(accountId)
                .apply { keepSynced(true) }
                .await<ProfileDbEntity>(exceptionHandler) ?: return null
        return converter.convert(profile)
    }

    override suspend fun getFirebaseToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return user.getIdToken(false).await(exceptionHandler).token
    }

    override suspend fun getFirebaseUserId(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return user.uid
    }

    override fun getAccountId(): String? = accountIdInternal

    override suspend fun getById(id: String): ShortAccountModel? {
        return db
                .child(TABLE_PUBLIC_ACCOUNTS)
                .child(id)
                .await<ShortAccountDbEntity>(exceptionHandler)
                ?.run { converter.convert(this) }

    }

    companion object {
        private const val EXTRA_PREFS_NAME = "USER_REPOSITORY_PREFS"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
    }
}
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
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.request.Ability
import com.mnassa.data.network.bean.retrofit.request.RegisterOrganizationAccountRequest
import com.mnassa.data.network.bean.retrofit.request.RegisterPersonalAccountRequest
import com.mnassa.data.network.bean.retrofit.request.RegisterSendingAccountInfoRequest
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.data.network.exception.handleException
import com.mnassa.domain.model.PersonalInfoModel
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
                interests = interests
        )).handleException(exceptionHandler)
        return converter.convert(result.account)
    }

    override suspend fun createOrganizationAccount(companyName: String, userName: String, city: String, offers: List<String>, interests: List<String>): ShortAccountModel {
        val result = firebaseAuthApi.registerOrganizationAccount(RegisterOrganizationAccountRequest(
                userName = userName,
                type = NetworkContract.AccountType.ORGANIZATION,
                offers = offers,
                interests = interests,
                organizationName = companyName
        )).handleException(exceptionHandler)
        return converter.convert(result.account)
    }

    override suspend fun processAccount(account: PersonalInfoModel) {
        firebaseAuthApi.registerSendAccountInfo(RegisterSendingAccountInfoRequest(
                account.birthdayDate,
                account.personalInfo?.lastName,
                account.userName,
                account.showContactEmail,
                account.language,
                account.accountType.name.toLowerCase(),
                account.birthday,
                account.contactPhone,
                converter.convertCollection(account.abilities, Ability::class.java),
                requireNotNull(getAccountId()),
                account.avatar,
                account.personalInfo?.firstName,
                account.showContactPhone,
                account.contactEmail,
                account.gender.name.toLowerCase()
        )).handleException(exceptionHandler)
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

    companion object {
        private const val EXTRA_PREFS_NAME = "USER_REPOSITORY_PREFS"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
    }
}
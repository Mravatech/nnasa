package com.mnassa.data.repository

import android.content.Context
import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.await
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.extensions.toValueChannel
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.api.FirebaseAuthApi
import com.mnassa.data.network.bean.firebase.InviteShortAccountDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.request.RegisterOrganizationAccountRequest
import com.mnassa.data.network.bean.retrofit.request.RegisterPersonalAccountRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.network.bean.retrofit.request.RegisterSendingAccountInfoRequest
import com.mnassa.domain.model.InvitedShortAccountModel
import com.mnassa.data.repository.DatabaseContract.TABLE_PUBLIC_ACCOUNTS
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

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
    override suspend fun getCurrentUserWithChannel(): ReceiveChannel<InvitedShortAccountModel>{
        val accountId = accountIdInternal
        return  db.child(DatabaseContract.TABLE_ACCOUNTS)
                .child(accountId)
                .toValueChannel<InviteShortAccountDbEntity>(exceptionHandler).map {
                    converter.convert<InvitedShortAccountModel>(requireNotNull(it))
                }
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

    override suspend fun processAccount(account: ShortAccountModel, path: String?) {
        //todo remove hardcode
        firebaseAuthApi.registerSendAccountInfo(RegisterSendingAccountInfoRequest(
                null,
                null,
                account.personalInfo!!.lastName,
                account.userName,
                true,
                "en",
                "personal",
                611265600000.0,
                account.contactPhone,
                null,
                null,
                getAccountId()!!,
                path,
                account.personalInfo!!.firstName,
                listOf("-L5o3gRz9DfDXkdTZ01B"),
                listOf("-L59C0y19-aGFdDN8kNc"),
                false
        )).await()
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
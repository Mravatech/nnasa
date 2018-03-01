package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.google.firebase.auth.FirebaseAuth
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.response.AccountResponseBean
import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.OrganizationAccountDiffModel
import com.mnassa.domain.model.PersonalAccountDiffModel
import com.mnassa.domain.model.impl.OrganizationAccountDiffModelImpl
import com.mnassa.domain.model.impl.PersonalAccountDiffModelImpl
import com.mnassa.domain.model.impl.ShortAccountModelImpl

/**
 * Created by Peter on 2/21/2018.
 */
class UserAccountConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertAccountFromDb)
        convertersContext.registerConverter(this::convertAccountFromRetrofit)
    }

    private fun convertAccountFromDb(input: ShortAccountDbEntity): ShortAccountModelImpl {

        var personalInfo: PersonalAccountDiffModel? = null
        var organizationInfo: OrganizationAccountDiffModel? = null
        val accountType: AccountType
        when (input.type) {
            NetworkContract.AccountType.ORGANIZATION -> {
                accountType = AccountType.ORGANIZATION
                organizationInfo = OrganizationAccountDiffModelImpl(
                        organizationName = requireNotNull(input.organizationName)
                )
            }
            NetworkContract.AccountType.PERSONAL -> {
                accountType = AccountType.PERSONAL
                personalInfo = PersonalAccountDiffModelImpl(
                        firstName = requireNotNull(input.firstName),
                        lastName = requireNotNull(input.lastName)
                )
            }
            else -> throw IllegalArgumentException("Illegal account type ${input.type}")
        }

        return ShortAccountModelImpl(
                id = input.id,
                firebaseUserId = requireNotNull(FirebaseAuth.getInstance().uid),
                userName = input.userName,
                accountType = accountType,
                avatar = input.avatar,
                contactPhone = null,
                language = null,
                organizationInfo = organizationInfo,
                personalInfo = personalInfo
        )
    }

    private fun convertAccountFromRetrofit(input: AccountResponseBean): ShortAccountModelImpl {
        var personalInfo: PersonalAccountDiffModel? = null
        var organizationInfo: OrganizationAccountDiffModel? = null
        val accountType: AccountType
        when (input.type) {
            NetworkContract.AccountType.ORGANIZATION -> {
                accountType = AccountType.ORGANIZATION
                organizationInfo = OrganizationAccountDiffModelImpl(
                        organizationName = requireNotNull(input.organizationName)
                )
            }
            NetworkContract.AccountType.PERSONAL -> {
                accountType = AccountType.PERSONAL
                personalInfo = PersonalAccountDiffModelImpl(
                        firstName = requireNotNull(input.firstName),
                        lastName = requireNotNull(input.lastName)
                )
            }
            else -> throw IllegalArgumentException("Illegal account type ${input.type}")
        }

        return ShortAccountModelImpl(
                id = input.id,
                firebaseUserId = requireNotNull(FirebaseAuth.getInstance().uid),
                userName = input.userName,
                accountType = accountType,
                avatar = null,
                contactPhone = input.contactPhone,
                language = input.language,
                organizationInfo = organizationInfo,
                personalInfo = personalInfo
        )
    }
}
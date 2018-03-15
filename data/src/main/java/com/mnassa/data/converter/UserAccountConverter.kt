package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.google.firebase.auth.FirebaseAuth
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.DeclinedShortAccountDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountAbilityDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.response.AccountResponseBean
import com.mnassa.domain.model.AccountAbility
import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.OrganizationAccountDiffModel
import com.mnassa.domain.model.PersonalAccountDiffModel
import com.mnassa.domain.model.impl.*
import java.util.*

/**
 * Created by Peter on 2/21/2018.
 */
class UserAccountConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertAccountFromDb)
        convertersContext.registerConverter(this::convertAccountFromRetrofit)
        convertersContext.registerConverter(this::convertAccountAbility)
        convertersContext.registerConverter(this::convertDeclined)
    }

    private fun convertAccountFromDb(input: ShortAccountDbEntity, token: Any?, convertersContext: ConvertersContext): ShortAccountModelImpl {

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
                personalInfo = personalInfo,
                abilities = convertersContext.convertCollection(input.abilitiesInternal ?: emptyList(), AccountAbility::class.java)
        )
    }

    private fun convertAccountFromRetrofit(input: AccountResponseBean, token: Any?, convertersContext: ConvertersContext): ShortAccountModelImpl {
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
                personalInfo = personalInfo,
                abilities = emptyList()
        )
    }

    private fun convertAccountAbility(input: ShortAccountAbilityDbEntity): AccountAbilityImpl {
        return AccountAbilityImpl(
                isMain = input.isMain,
                name = input.name,
                place = input.place
        )
    }

    private fun convertDeclined(input: DeclinedShortAccountDbEntity, token: Any?, convertersContext: ConvertersContext): DeclinedShortAccountModelImpl {
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

        return DeclinedShortAccountModelImpl(
                id = input.id,
                firebaseUserId = requireNotNull(FirebaseAuth.getInstance().uid),
                userName = input.userName,
                accountType = accountType,
                avatar = input.avatar,
                contactPhone = null,
                language = null,
                organizationInfo = organizationInfo,
                personalInfo = personalInfo,
                abilities = convertersContext.convertCollection(input.abilitiesInternal ?: emptyList(), AccountAbility::class.java),
                declinedAt = Date(input.declinedAt)
        )
    }
}
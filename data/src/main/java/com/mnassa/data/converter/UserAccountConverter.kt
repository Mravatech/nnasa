package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.DeclinedShortAccountDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountAbilityDbEntity
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.network.bean.retrofit.response.AccountResponseBean
import com.mnassa.domain.model.*
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
                        organizationName = input.organizationName ?: CONVERT_ERROR_MESSAGE
                )
            }
            NetworkContract.AccountType.PERSONAL -> {
                accountType = AccountType.PERSONAL
                personalInfo = PersonalAccountDiffModelImpl(
                    firstName = input.firstName ?: CONVERT_ERROR_MESSAGE,
                    lastName = input.lastName ?: CONVERT_ERROR_MESSAGE
                )
            }
            else -> {
                accountType = AccountType.PERSONAL
                personalInfo = PersonalAccountDiffModelImpl(
                    firstName = input.firstName ?: CONVERT_ERROR_MESSAGE,
                    lastName = input.lastName ?: CONVERT_ERROR_MESSAGE
                )
            }
        }

        return ShortAccountModelImpl(
                id = input.id,
                serialNumber = input.serialNumber,
                userName = input.userName ?: CONVERT_ERROR_MESSAGE,
                accountType = accountType,
                avatar = input.avatar,
                contactPhone = null,
                language = null,
                organizationInfo = organizationInfo,
                personalInfo = personalInfo,
                abilities = convertersContext.convertCollection(input.abilitiesInternal
                        ?: emptyList(), AccountAbility::class.java),
                connectedBy = input.connectedBy?.run { convertersContext.convert(this, ConnectedByModel::class.java) }
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
                        organizationName = input.organizationName ?: CONVERT_ERROR_MESSAGE
                )
            }
            NetworkContract.AccountType.PERSONAL -> {
                accountType = AccountType.PERSONAL
                personalInfo = PersonalAccountDiffModelImpl(
                    firstName = input.firstName ?: CONVERT_ERROR_MESSAGE,
                    lastName = input.lastName ?: CONVERT_ERROR_MESSAGE
                )
            }
            else -> {
                accountType = AccountType.PERSONAL
                personalInfo = PersonalAccountDiffModelImpl(
                    firstName = input.firstName ?: CONVERT_ERROR_MESSAGE,
                    lastName = input.lastName ?: CONVERT_ERROR_MESSAGE
                )
            }
        }

        return ShortAccountModelImpl(
                id = input.id,
                serialNumber = input.serialNumber,
                userName = input.userName,
                accountType = accountType,
                avatar = null,
                contactPhone = input.contactPhone,
                language = input.language,
                organizationInfo = organizationInfo,
                personalInfo = personalInfo,
                abilities = emptyList(),
                connectedBy = input.connectedBy?.run { convertersContext.convert(this, ConnectedByModel::class.java) }
        )
    }

    private fun convertAccountAbility(input: ShortAccountAbilityDbEntity): AccountAbilityImpl {
        return AccountAbilityImpl(
                isMain = input.isMain ?: AccountAbility.DEFAULT_IS_MAIN,
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
                        organizationName = input.organizationName ?: CONVERT_ERROR_MESSAGE
                )
            }
            NetworkContract.AccountType.PERSONAL -> {
                accountType = AccountType.PERSONAL
                personalInfo = PersonalAccountDiffModelImpl(
                    firstName = input.firstName ?: CONVERT_ERROR_MESSAGE,
                    lastName = input.lastName ?: CONVERT_ERROR_MESSAGE
                )
            }
            else -> {
                accountType = AccountType.PERSONAL
                personalInfo = PersonalAccountDiffModelImpl(
                    firstName = input.firstName ?: CONVERT_ERROR_MESSAGE,
                    lastName = input.lastName ?: CONVERT_ERROR_MESSAGE
                )
            }
        }

        return DeclinedShortAccountModelImpl(
                id = input.id,
                serialNumber = input.serialNumber,
                userName = input.userName ?: CONVERT_ERROR_MESSAGE,
                accountType = accountType,
                avatar = input.avatar,
                contactPhone = null,
                language = null,
                organizationInfo = organizationInfo,
                personalInfo = personalInfo,
                abilities = convertersContext.convertCollection(input.abilitiesInternal
                        ?: emptyList(), AccountAbility::class.java),
                declinedAt = Date(input.declinedAt),
                connectedBy = input.connectedBy?.run { convertersContext.convert(this, ConnectedByModel::class.java) }
        )
    }
}
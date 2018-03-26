package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.google.firebase.auth.FirebaseAuth
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.ProfileDbEntity
import com.mnassa.domain.model.AccountAbility
import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.OrganizationAccountDiffModel
import com.mnassa.domain.model.PersonalAccountDiffModel
import com.mnassa.domain.model.impl.OrganizationAccountDiffModelImpl
import com.mnassa.domain.model.impl.PersonalAccountDiffModelImpl
import com.mnassa.domain.model.impl.ProfileAccountModelImpl

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

class ProfileConverter : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertAccountFromDb)
    }

    private fun convertAccountFromDb(input: ProfileDbEntity, token: Any?, convertersContext: ConvertersContext): ProfileAccountModelImpl {

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

        return ProfileAccountModelImpl(
                createdAt = input.createdAt,
                id = input.id,
                createdAtDate = input.createdAtDate,
                firebaseUserId = requireNotNull(FirebaseAuth.getInstance().uid),
                interests = input.interests,
                offers = input.offers,
                userName = input.userName,
                points = input.points,
                accountType = accountType,
                totalIncome = input.totalIncome,
                totalOutcome = input.totalOutcome,
                avatar = input.avatar,
                contactPhone = null,
                language = null,
                organizationInfo = organizationInfo,
                personalInfo = personalInfo,
                abilities = convertersContext.convertCollection(input.abilitiesInternal ?: emptyList(), AccountAbility::class.java)
        )
    }

//    override val createdAt: Long?,
//    override var id: String,
//    override val createdAtDate: String?,
//    override var firebaseUserId: String,
//    override val interests: List<String>?,
//    override val offers: List<String>?,
//    override var userName: String,
//    override val points: Int?,
//    override var accountType: AccountType,
//    override val totalIncome: Int?,
//    override var avatar: String?,
//    override val totalOutcome: Int?,
//    override var contactPhone: String?,
//    override var language: String?,
//    override var personalInfo: PersonalAccountDiffModel?,
//    override var organizationInfo: OrganizationAccountDiffModel?,
//    override var abilities: List<AccountAbility>) : ProfileAccountModel

}
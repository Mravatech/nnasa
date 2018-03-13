package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModelTemp
import com.mnassa.domain.repository.TagRepository
import com.mnassa.domain.repository.UserRepository

/**
 * Created by Peter on 2/21/2018.
 */
class UserProfileInteractorImpl(
        private val userRepository: UserRepository,
        private val tagRepository: TagRepository
) : UserProfileInteractor {

    override suspend fun getProfile(): ShortAccountModel {
        return requireNotNull(userRepository.getCurrentUser())
    }

    override suspend fun createPersonalAccount(firstName: String,
                                               secondName: String,
                                               userName: String,
                                               city: String,
                                               offers: List<TagModelTemp>,
                                               interests: List<TagModelTemp>
    ): ShortAccountModel {
        val offersWithIds = getFilteredTags(offers)
        val interestsWithIds = getFilteredTags(interests)
        val account = userRepository.createPersonAccount(
                firstName = firstName,
                secondName = secondName,
                userName = userName,
                city = city,
                offers = offersWithIds,
                interests = interestsWithIds)
        userRepository.setCurrentUserAccount(account)
        return account
    }

    override suspend fun createOrganizationAccount(companyName: String, userName: String, city: String, offers: List<TagModelTemp>, interests: List<TagModelTemp>): ShortAccountModel {
        val offersWithIds = getFilteredTags(offers)
        val interestsWithIds = getFilteredTags(interests)
        val account = userRepository.createOrganizationAccount(
                companyName = companyName,
                userName = userName,
                city = city,
                offers = offersWithIds,
                interests = interestsWithIds
        )
        userRepository.setCurrentUserAccount(account)
        return account
    }

    override suspend fun processAccount(account: ShortAccountModel, path: String?) {
//todo handle response
        userRepository.processAccount(account, path)
    }

    override suspend fun setCurrentUserAccount(account: ShortAccountModel) {
        userRepository.setCurrentUserAccount(account)
    }

    override suspend fun getToken(): String? = userRepository.getFirebaseToken()
    override suspend fun getAccountId(): String? = userRepository.getAccountId()

    private suspend fun getFilteredTags(tagsWithCustom: List<TagModelTemp>): List<String> {
        val customTags = tagsWithCustom.filter { it.id == null }.map { it.name }
        val existsTags = tagsWithCustom.filter { it.id != null }.map { it.id!! }
        val tags = arrayListOf<String>()
        if (!customTags.isEmpty()) {
            val newTags = tagRepository.createCustomTagIds(customTags)//todo handle error
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }

}
package com.mnassa.screen.registration

import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by Peter on 2/26/2018.
 */
class RegistrationViewModelImpl(
        private val userProfileInteractor: UserProfileInteractor,
        private val tagInteractor: TagInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor
) : MnassaViewModelImpl(), RegistrationViewModel {

    override val openScreenChannel: ArrayBroadcastChannel<RegistrationViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)
    override fun registerPerson(userName: String, city: String, firstName: String, secondName: String, offers: List<TagModel>, interests: List<TagModel>) {
        handleException {
            withProgressSuspend {
                val offersWithIds = getFilteredTags(offers)
                val interestsWithIds = getFilteredTags(interests)
                val shortAccountModel = userProfileInteractor.createPersonalAccount(
                        firstName = firstName,
                        secondName = secondName,
                        userName = userName,
                        city = city,
                        offers = offersWithIds,
                        interests = interestsWithIds
                )
                openScreenChannel.send(RegistrationViewModel.OpenScreenCommand.PersonalInfoScreen(shortAccountModel))
            }
        }
    }

    override fun registerOrganization(userName: String, city: String, companyName: String, offers: List<TagModel>, interests: List<TagModel>) {
        handleException {
            withProgressSuspend {
                val offersWithIds = getFilteredTags(offers)
                val interestsWithIds = getFilteredTags(interests)
                userProfileInteractor.createOrganizationAccount(
                        companyName = companyName,
                        userName = userName,
                        city = city,
                        offers = offersWithIds,
                        interests = interestsWithIds
                )
            }
            openScreenChannel.send(RegistrationViewModel.OpenScreenCommand.OrganizationInfoScreen())
        }
    }

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderInteractor.getReqieredPlaces(constraint)
    }

    override suspend fun search(search: String): List<TagModel> {
        return tagInteractor.search(search)
    }

    private suspend fun getFilteredTags(customTagsAndTagsWithIds: List<TagModel>): List<String> {
        val customTags = customTagsAndTagsWithIds.filter { it.id == null }.map { it.name }
        val existsTags = customTagsAndTagsWithIds.mapNotNull { it.id }
        val tags = arrayListOf<String>()
        if (customTags.isNotEmpty()) {
            val newTags = tagInteractor.createCustomTagIds(customTags)
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }

}
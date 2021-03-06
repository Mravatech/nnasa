package com.mnassa.screen.registration

import com.mnassa.core.addons.asyncWorker
import com.mnassa.core.addons.consumeTo
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by Peter on 2/26/2018.
 */
class RegistrationViewModelImpl(
        private val userProfileInteractor: UserProfileInteractor,
        private val tagInteractor: TagInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor
) : MnassaViewModelImpl(), RegistrationViewModel {

    override val openScreenChannel: BroadcastChannel<RegistrationViewModel.OpenScreenCommand> = BroadcastChannel(10)
    override val addTagRewardChannel: BroadcastChannel<Long?> = ConflatedBroadcastChannel()

    private val isInterestsMandatory = GlobalScope.asyncWorker { tagInteractor.isInterestsMandatory() }
    private val isOffersMandatory = GlobalScope.asyncWorker { tagInteractor.isOffersMandatory() }
    private val createAccountMutex = Mutex()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            tagInteractor.getAddTagPrice().consumeTo(addTagRewardChannel)
        }
    }

    override suspend fun hasPersonalAccountChannel(): Boolean {
        return withProgressSuspend {
            handleExceptionsSuspend { userProfileInteractor.getAllAccounts().consume { receive() }.any { it.accountType == AccountType.PERSONAL } }
                    ?: true
        }
    }

    override suspend fun registerPerson(userName: String, city: String, firstName: String, secondName: String, offers: List<TagModel>, interests: List<TagModel>) {
        createAccountMutex.withLock {
            handleExceptionsSuspend {
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
                    val profile = userProfileInteractor.getProfileById(shortAccountModel.id)
                            ?: return@withProgressSuspend
                    openScreenChannel.send(RegistrationViewModel.OpenScreenCommand.PersonalInfoScreen(profile))
                }
            }
        }
    }

    override suspend fun registerOrganization(userName: String, city: String, companyName: String, offers: List<TagModel>, interests: List<TagModel>) {
        createAccountMutex.withLock {
            handleExceptionsSuspend {
                withProgressSuspend {
                    val offersWithIds = getFilteredTags(offers)
                    val interestsWithIds = getFilteredTags(interests)
                    val shortAccountModel = userProfileInteractor.createOrganizationAccount(
                            companyName = companyName,
                            userName = userName,
                            city = city,
                            offers = offersWithIds,
                            interests = interestsWithIds
                    )
                    val profile = userProfileInteractor.getProfileById(shortAccountModel.id)
                            ?: return@withProgressSuspend
                    openScreenChannel.send(RegistrationViewModel.OpenScreenCommand.OrganizationInfoScreen(profile))
                }
            }
        }
    }

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> = placeFinderInteractor.getReqieredPlaces(constraint)

    override suspend fun isInterestsMandatory(): Boolean = isInterestsMandatory.await()

    override suspend fun isOffersMandatory(): Boolean = isOffersMandatory.await()

    private suspend fun getFilteredTags(customTagsAndTagsWithIds: List<TagModel>): List<String> {
        val customTags = customTagsAndTagsWithIds.filter { it.id == null }.map { it.name }
        val existsTags = customTagsAndTagsWithIds.mapNotNull { it.id }
        val tags = arrayListOf<String>()
        if (customTags.isNotEmpty()) {
            val newTags = tagInteractor.createCustomTagIds(customTags.map { it.toString() })
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }
}
package com.mnassa.screen.profile.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.formatted
import com.mnassa.screen.posts.need.details.adapter.PostTagRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_header_profile_personal_view.view.*
import kotlinx.android.synthetic.main.sub_header_personal.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */

//TODO: refactor it
class PersonalProfileViewHolder(
        itemView: View,
        private val onClickListener: View.OnClickListener,
        item: ProfileAccountModel) : BaseProfileHolder(itemView) {

    override fun bind(item: PostModel) {
    }

    init {
        bindProfile(item)
    }

    override fun bindProfile(profile: ProfileAccountModel) {
        with(itemView) {
            tvProfileConnections.text = getSpannableText(profile.numberOfConnections.toString(), fromDictionary(R.string.profile_connections), Color.BLACK)
            tvPointsGiven.text = getSpannableText(profile.visiblePoints.toString(), fromDictionary(R.string.profile_points_given), Color.BLACK)
            profile.location?.let {
                tvProfileLocation.text = it.formatted()
                tvProfileLocation.visibility = View.VISIBLE
            }
            setCheckedTexts(tvLabelProfilePhone, tvProfilePhone, vTopProfilePhone, fromDictionary(R.string.profile_mobile_phone), profile.contactPhone)
            setCheckedTexts(tvLabelProfileEmail, tvProfileEmail, vTopProfileEmail, fromDictionary(R.string.profile_email), profile.contactEmail)
            setCheckedTexts(tvLabelDateOfBirth, tvDateOfBirth, vTopProfileDateOfBirth, fromDictionary(R.string.profile_date_of_birth), getDateByTimeMillis(profile.createdAt))


            tvMoreInformation.text = fromDictionary(R.string.profile_more_information)
            flMoreInformation.setOnClickListener {
                onMoreClick(profileInfo = profileInfo,
                        llBottomTags = llBottomTags,
                        tvMoreInformation = tvMoreInformation,
                        vBottomDivider = vBottomDivider,
                        areThereTags = profile.offers.isNotEmpty())
            }
            vBottomDivider.visibility = if (profile.offers.isEmpty()) View.VISIBLE else View.GONE

            tvProfileConnections.setOnClickListener(onClickListener)
            tvProfileConnections.tag = this@PersonalProfileViewHolder
            tvPointsGiven.setOnClickListener(onClickListener)
            tvPointsGiven.tag = this@PersonalProfileViewHolder
        }
    }

    override fun bindOffers(offers: List<TagModel>) {
        with(itemView) {
            llBottomTags.setTags(offers)
            setCheckedTags(tvProfileCanHelpWith, chipProfileCanHelpWith, vTopProfileCanHelpWith, offers, fromDictionary(R.string.reg_account_can_help_with))
        }
    }

    override fun bindInterests(interests: List<TagModel>) {
        with(itemView) {
            setCheckedTags(tvProfileInterestedIn, chipProfileInterestWith, vTopProfileInterestedIn, interests, fromDictionary(R.string.reg_account_interested_in))
        }
    }

    override fun bindConnectionStatus(connectionStatus: ConnectionStatus) = Unit

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener, profileModel: ProfileAccountModel): PersonalProfileViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_profile_personal_view, parent, false)
            return PersonalProfileViewHolder(view, onClickListener, profileModel)
        }
    }

}
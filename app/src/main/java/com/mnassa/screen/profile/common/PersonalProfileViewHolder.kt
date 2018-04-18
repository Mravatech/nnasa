package com.mnassa.screen.profile.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.formatted
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_header_profile_personal_view.view.*
import kotlinx.android.synthetic.main.sub_header_personal.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
class PersonalProfileViewHolder(
        itemView: View,
        private val onClickListener: View.OnClickListener,
        item: ProfileModel) : BaseProfileHolder(itemView) {
    override fun bind(item: PostModel) {
    }

    init {
        with(itemView) {
            tvProfileConnections.text = getSpannableText(item.profile.numberOfConnections.toString(), fromDictionary(R.string.profile_connections), Color.BLACK)
            tvPointsGiven.text = getSpannableText(item.profile.visiblePoints.toString(), fromDictionary(R.string.profile_points_given), Color.BLACK)
            item.profile.location?.let {
                tvProfileLocation.text = it.formatted()
                tvProfileLocation.visibility = View.VISIBLE
            }
            setCheckedTexts(tvLabelProfilePhone, tvProfilePhone, vTopProfilePhone, fromDictionary(R.string.profile_mobile_phone), item.profile.contactPhone)
            setCheckedTexts(tvLabelProfileEmail, tvProfileEmail, vTopProfileEmail, fromDictionary(R.string.profile_email), item.profile.contactEmail)
            setCheckedTexts(tvLabelDateOfBirth, tvDateOfBirth, vTopProfileDateOfBirth, fromDictionary(R.string.profile_date_of_birth), getDateByTimeMillis(item.profile.createdAt))
            setCheckedTags(tvProfileCanHelpWith, chipProfileCanHelpWith, vTopProfileCanHelpWith, item.offers, fromDictionary(R.string.reg_account_can_help_with))
            setCheckedTags(tvProfileInterestedIn, chipProfileInterestWith, vTopProfileInterestedIn, item.interests, fromDictionary(R.string.reg_account_interested_in))
            tvMoreInformation.text = fromDictionary(R.string.profile_more_information)
            flMoreInformation.setOnClickListener {
                onMoreClick(profileInfo = profileInfo,
                        llBottomTags = llBottomTags,
                        tvMoreInformation = tvMoreInformation,
                        vBottomDivider = vBottomDivider,
                        areThereTags = item.offers.isNotEmpty())
            }
            vBottomDivider.visibility = if (item.offers.isEmpty()) View.VISIBLE else View.GONE
            llBottomTags.setTags(item.offers)
            tvProfileConnections.setOnClickListener(onClickListener)
            tvProfileConnections.tag = this@PersonalProfileViewHolder
            tvPointsGiven.setOnClickListener(onClickListener)
            tvPointsGiven.tag = this@PersonalProfileViewHolder
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener, profileModel: ProfileModel): PersonalProfileViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_profile_personal_view, parent, false)
            return PersonalProfileViewHolder(view, onClickListener, profileModel)
        }
    }

}
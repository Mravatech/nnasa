package com.mnassa.screen.profile.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.formatted
import com.mnassa.screen.profile.ProfileViewModel
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_header_another_profile_company_view.view.*
import kotlinx.android.synthetic.main.sub_header_company.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */

class AnotherCompanyProfileHolder(
        itemView: View,
        private val viewModel: ProfileViewModel,
        item: ProfileModel) : BaseProfileHolder(itemView) {
    override fun bind(item: PostModel) {
    }

    init {
        with(itemView) {
            tvProfileConnections.text = getSpannableText(item.profile.numberOfConnections.toString(), fromDictionary(R.string.profile_connections), Color.BLACK)
            tvPointsGiven.text = getSpannableText(item.profile.visiblePoints.toString(), fromDictionary(R.string.profile_points_given), Color.BLACK)
            handleConnection(tvConnectionStatus, item.connectionStatus)
            if (item.connectionStatus != ConnectionStatus.NONE) {
                tvConnectionStatus.setOnClickListener {
                    viewModel.connectionStatusClick(item.connectionStatus)
                }
            }
            item.profile.location?.let {
                tvProfileLocation.text = it.formatted()
                tvProfileLocation.visibility = View.VISIBLE
            }
            setCheckedTexts(tvLabelProfileWebSite, tvProfileWebSite, vTopProfileWebSite, fromDictionary(R.string.profile_website), item.profile.website)
            setCheckedTexts(tvLabelProfileEmail, tvProfileEmail, vTopProfileEmail, fromDictionary(R.string.profile_email), item.profile.contactEmail)
            setCheckedTexts(tvLabelDateOfFoundation, tvDateOfFoundation, vTopProfileDateOfFoundation, fromDictionary(R.string.profile_date_of_foundation), getDateByTimeMillis(item.profile.createdAt))
            setCheckedTexts(tvLabelProfilePhone, tvProfilePhone, vTopProfilePhone, fromDictionary(R.string.profile_mobile_phone), item.profile.contactPhone)
            setCheckedTags(tvProfileWeCanHelpWith, chipProfileWeCanHelpWith, vTopProfileWeCanHelpWith, item.offers, fromDictionary(R.string.reg_account_can_help_with))
            setCheckedTags(tvProfileOurInterestedIn, chipProfileOurInterestWith, vTopProfileOurInterestedIn, item.interests, fromDictionary(R.string.reg_account_interested_in))
            tvMoreInformation.text = fromDictionary(R.string.profile_more_information)
            flMoreInformation.setOnClickListener {
                onMoreClick(profileInfo = profileInfo,
                        llBottomTags = llBottomTags,
                        tvMoreInformation =  tvMoreInformation,
                        vBottomDivider =  vBottomDivider,
                        areThereTags =  item.offers.isNotEmpty())
            }
            vBottomDivider.visibility = if (item.offers.isEmpty()) View.VISIBLE else View.GONE
//            item.offers.let {
//                for (tag in it) {
//                    llBottomTags.addView(SimpleChipView(llBottomTags.context, tag))
//                }
//            }
            llBottomTags.setTags(item.offers)
        }
    }

//    private fun onConnectionsClick(connectionStatus: ConnectionStatus) {
//
//    }

    companion object {
        fun newInstance(parent: ViewGroup, viewModel: ProfileViewModel, profileModel: ProfileModel): AnotherCompanyProfileHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_another_profile_company_view, parent, false)
            return AnotherCompanyProfileHolder(view, viewModel, profileModel)
        }
    }
}
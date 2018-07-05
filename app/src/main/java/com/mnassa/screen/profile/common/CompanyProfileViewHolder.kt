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
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_header_profile_company_view.view.*
import kotlinx.android.synthetic.main.sub_header_company.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */

class CompanyProfileViewHolder(
        itemView: View,
        private val onClickListener: View.OnClickListener,
        item: ProfileAccountModel) : BaseProfileHolder(itemView) {
    override fun bind(item: PostModel) = Unit

    init {
        bindProfile(item)
        itemView.rvBottomTags.adapter = bottomTagsAdapter
    }

    override fun bindProfile(profile: ProfileAccountModel) {
        with(itemView) {
            tvProfileConnections.text = getSpannableText(profile.numberOfConnections.toString(), fromDictionary(R.string.profile_connections), Color.BLACK)
            tvPointsGiven.text = getSpannableText(profile.visiblePoints.toString(), fromDictionary(R.string.profile_points_given), Color.BLACK)
            profile.location?.let {
                tvProfileLocation.text = it.formatted()
                tvProfileLocation.visibility = View.VISIBLE
            }
            setCheckedTexts(tvLabelProfileWebSite, tvProfileWebSite, vTopProfileWebSite, fromDictionary(R.string.profile_website), profile.website)
            setCheckedTexts(tvLabelProfileEmail, tvProfileEmail, vTopProfileEmail, fromDictionary(R.string.profile_email), profile.contactEmail)
            setCheckedTexts(tvLabelDateOfFoundation, tvDateOfFoundation, vTopProfileDateOfFoundation, fromDictionary(R.string.profile_date_of_foundation), getDateByTimeMillis(profile.createdAt))
            setCheckedTexts(tvLabelProfilePhone, tvProfilePhone, vTopProfilePhone, fromDictionary(R.string.profile_mobile_phone), profile.contactPhone)

            tvMoreInformation.text = fromDictionary(R.string.profile_more_information)
            flMoreInformation.setOnClickListener {
                onMoreClick(profileInfo = profileInfo,
                        llBottomTags = rvBottomTags,
                        tvMoreInformation = tvMoreInformation,
                        vBottomDivider = vBottomDivider,
                        areThereTags = profile.offers.isNotEmpty())
            }
            vBottomDivider.visibility = if (profile.offers.isEmpty()) View.VISIBLE else View.GONE

            tvProfileConnections.setOnClickListener(onClickListener)
            tvProfileConnections.tag = this@CompanyProfileViewHolder
            tvPointsGiven.setOnClickListener(onClickListener)
            tvPointsGiven.tag = this@CompanyProfileViewHolder
        }
    }

    override fun bindOffers(offers: List<TagModel>) {
        super.bindOffers(offers)
        with(itemView) {
            setCheckedTags(tvProfileWeCanHelpWith, chipProfileWeCanHelpWith, vTopProfileWeCanHelpWith, offers, fromDictionary(R.string.reg_account_can_help_with))
        }
    }

    override fun bindInterests(interests: List<TagModel>) {
        with(itemView) {
            setCheckedTags(tvProfileOurInterestedIn, chipProfileOurInterestWith, vTopProfileOurInterestedIn, interests, fromDictionary(R.string.reg_account_interested_in))
        }
    }

    override fun bindConnectionStatus(connectionStatus: ConnectionStatus) = Unit

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener, profileModel: ProfileAccountModel): CompanyProfileViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_profile_company_view, parent, false)
            return CompanyProfileViewHolder(view, onClickListener, profileModel)
        }
    }
}
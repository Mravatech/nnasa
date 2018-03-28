package com.mnassa.screen.profile.common

import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.impl.TagModelImpl
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.SimpleChipView
import kotlinx.android.synthetic.main.item_header_another_profile_company_view.view.*
import kotlinx.android.synthetic.main.item_sub_header_company.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */

class AnotherCompanyProfileHolder(itemView: View) : BaseProfileHolder(itemView) {
    override fun bind(item: ProfileModel) {
        with(itemView) {
            tvProfileConnections.text = getSpannableText(item.profile.numberOfConnections.toString(), fromDictionary(R.string.profile_connections))
            tvPointsGiven.text = getSpannableText(item.profile.visiblePoints.toString(), fromDictionary(R.string.profile_points_given))
            tvConnectionStatus.text = getSpannableText(" ", "Connected")
            tvProfileLocation.text = item.profile.location?.en?.placeName
            setCheckedTexts(tvLabelProfileWebSite, tvProfileWebSite, vTopProfileWebSite, fromDictionary(R.string.profile_mobile_phone), item.profile.contactPhone)
            setCheckedTexts(tvLabelProfileEmail, tvProfileEmail, vTopProfileEmail, fromDictionary(R.string.profile_email), item.profile.contactEmail)
            setCheckedTexts(tvLabelDateOfFoundation, tvDateOfFoundation, vTopProfileDateOfFoundation, fromDictionary(R.string.profile_date_of_foundation), getDateByTimeMillis(item.profile.createdAt))
            setCheckedTexts(tvLabelProfilePhone, tvProfilePhone, vTopProfilePhone, fromDictionary(R.string.profile_mobile_phone), item.profile.contactPhone)
            setCheckedTags(tvProfileWeCanHelpWith, chipProfileWeCanHelpWith, vTopProfileWeCanHelpWith, item.offers, fromDictionary(R.string.reg_account_can_help_with))
            setCheckedTags(tvProfileOurInterestedIn, chipProfileOurInterestWith, vTopProfileOurInterestedIn, item.interests, fromDictionary(R.string.reg_account_interested_in))
            tvMoreInformation.text = fromDictionary(R.string.profile_more_information)
            flMoreInformation.setOnClickListener {
                profileInfo.visibility = if (profileInfo.visibility == View.GONE) View.VISIBLE else View.GONE
                flTags.visibility = if (flTags.visibility == View.GONE) View.VISIBLE else View.GONE
                val drawable = if (profileInfo.visibility == View.GONE) R.drawable.ic_down else R.drawable.ic_up
                val img = ResourcesCompat.getDrawable(resources, drawable, null)
                tvMoreInformation.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
            }
            item.profile.offers?.let {
                for (tag in it) {
                    flTags.addView(SimpleChipView(flTags.context, TagModelImpl(null, tag, null)))
                }
            }
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup): AnotherCompanyProfileHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_another_profile_company_view, parent, false)
            val viewHolder = AnotherCompanyProfileHolder(view)
            return viewHolder
        }
    }
}
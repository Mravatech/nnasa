package com.mnassa.screen.profile.common

import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.impl.TagModelImpl
import com.mnassa.extensions.formatted
import com.mnassa.screen.profile.ProfileViewModel
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.SimpleChipView
import kotlinx.android.synthetic.main.item_header_profile_personal_view.view.*
import kotlinx.android.synthetic.main.sub_header_personal.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
class PersonalProfileViewHolder(
        itemView: View,
        private val viewModel: ProfileViewModel,
        item: ProfileModel) : BaseProfileHolder(itemView) {
    override fun bind(item: PostModel) {
//        with(itemView) {
//            tvProfileConnections.text = getSpannableText(item.profile.numberOfConnections.toString(), fromDictionary(R.string.profile_connections))
//            tvPointsGiven.text = getSpannableText(item.profile.visiblePoints.toString(), fromDictionary(R.string.profile_points_given))
//            item.profile.location?.let {
//                tvProfileLocation.text = it.formatted()
//                tvProfileLocation.visibility = View.VISIBLE
//            }
//            setCheckedTexts(tvLabelProfilePhone, tvProfilePhone, vTopProfilePhone, fromDictionary(R.string.profile_mobile_phone), item.profile.contactPhone)
//            setCheckedTexts(tvLabelProfileEmail, tvProfileEmail, vTopProfileEmail, fromDictionary(R.string.profile_email), item.profile.contactEmail)
//            setCheckedTexts(tvLabelDateOfBirth, tvDateOfBirth, vTopProfileDateOfBirth, fromDictionary(R.string.profile_date_of_birth), getDateByTimeMillis(item.profile.createdAt))
//            setCheckedTags(tvProfileCanHelpWith, chipProfileCanHelpWith, vTopProfileCanHelpWith, item.offers, fromDictionary(R.string.reg_account_can_help_with))
//            setCheckedTags(tvProfileInterestedIn, chipProfileInterestWith, vTopProfileInterestedIn, item.interests, fromDictionary(R.string.reg_account_interested_in))
//            tvMoreInformation.text = fromDictionary(R.string.profile_more_information)
//            flMoreInformation.setOnClickListener {
//                profileInfo.visibility = if (profileInfo.visibility == View.GONE) View.VISIBLE else View.GONE
//                flTags.visibility = if (flTags.visibility == View.GONE) View.VISIBLE else View.GONE
//                val drawable = if (profileInfo.visibility == View.GONE) R.drawable.ic_down else R.drawable.ic_up
//                val img = ResourcesCompat.getDrawable(resources, drawable, null)
//                tvMoreInformation.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
//                tvMoreInformation.text = if (profileInfo.visibility == View.GONE) {
//                    fromDictionary(R.string.profile_more_information)
//                } else {
//                    fromDictionary(R.string.profile_less_information)
//                }
//            }
//            item.profile.offers?.let {
//                for (tag in it) {
//                    flTags.addView(SimpleChipView(flTags.context, TagModelImpl(null, tag, null)))
//                }
//            }
//            tvProfileConnections.setOnClickListener { viewModel.connectionClick() }
//            tvPointsGiven.setOnClickListener { viewModel.walletClick() }
//        }
    }

    init {
        with(itemView) {
            tvProfileConnections.text = getSpannableText(item.profile.numberOfConnections.toString(), fromDictionary(R.string.profile_connections))
            tvPointsGiven.text = getSpannableText(item.profile.visiblePoints.toString(), fromDictionary(R.string.profile_points_given))
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
                profileInfo.visibility = if (profileInfo.visibility == View.GONE) View.VISIBLE else View.GONE
                flTags.visibility = if (flTags.visibility == View.GONE) View.VISIBLE else View.GONE
                val drawable = if (profileInfo.visibility == View.GONE) R.drawable.ic_down else R.drawable.ic_up
                val img = ResourcesCompat.getDrawable(resources, drawable, null)
                tvMoreInformation.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                tvMoreInformation.text = if (profileInfo.visibility == View.GONE) {
                    fromDictionary(R.string.profile_more_information)
                } else {
                    fromDictionary(R.string.profile_less_information)
                }
            }
            item.profile.offers?.let {
                for (tag in it) {
                    flTags.addView(SimpleChipView(flTags.context, TagModelImpl(null, tag, null)))
                }
            }
            tvProfileConnections.setOnClickListener { viewModel.connectionClick() }
            tvPointsGiven.setOnClickListener { viewModel.walletClick() }
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup, viewModel: ProfileViewModel, profileModel: ProfileModel): PersonalProfileViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_profile_personal_view, parent, false)
            return PersonalProfileViewHolder(view, viewModel,profileModel)
        }
    }

}
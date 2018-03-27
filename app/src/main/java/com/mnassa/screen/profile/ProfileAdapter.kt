package com.mnassa.screen.profile

import android.graphics.Color
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mnassa.R
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.TagModelImpl
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.FlowLayout
import com.mnassa.widget.SimpleChipView
import kotlinx.android.synthetic.main.item_header_profile_view.view.*
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

class ProfileAdapter() : BasePaginationRVAdapter<ProfileModel>() {

    private val selectedAccountsInternal: MutableList<ProfileModel> = mutableListOf()
    var data: List<ProfileModel>
        get() = selectedAccountsInternal
        set(value) {
            selectedAccountsInternal.clear()
            selectedAccountsInternal.addAll(value)

            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<ProfileModel> {
        return ProfileViewHolder.newInstance(parent, selectedAccountsInternal)

    }

    private class ProfileViewHolder(private val selectedAccount: List<ProfileModel>, itemView: View) : BaseVH<ProfileModel>(itemView) {
        override fun bind(item: ProfileModel) {
            with(itemView) {
                tvProfileConnections.text = getSpannableText(item.profile.numberOfConnections.toString(), fromDictionary(R.string.profile_connections))
                tvPointsGiven.text = getSpannableText(item.profile.visiblePoints.toString(), fromDictionary(R.string.profile_points_given))
                setCheckedTexts(tvLabelProfilePhone, tvProfilePhone, vTopProfilePhone, fromDictionary(R.string.profile_mobile_phone), item.profile.contactPhone)
                setCheckedTexts(tvLabelProfileEmail, tvProfileEmail, vTopProfileEmail, fromDictionary(R.string.profile_email), item.profile.contactEmail)
                setCheckedTexts(tvLabelDateOfBirth, tvDateOfBirth, vTopProfileDateOfBirth, fromDictionary(R.string.profile_date_of_birth), getDateByTimeMillis(item.profile.createdAt))
                setCheckedTags(tvProfileCanHelpWith, chipProfileCanHelpWith, null, item.offers, fromDictionary(R.string.reg_account_can_help_with))
                setCheckedTags(tvProfileInterestedIn, chipProfileInterestWith, vTopProfileInterestedIn, item.interests, fromDictionary(R.string.reg_account_interested_in))
                tvMoreInformation.text = fromDictionary(R.string.profile_more_information)
                flMoreInformation.setOnClickListener {
                    profileInfo.visibility = if (profileInfo.visibility == View.GONE) View.VISIBLE else View.GONE
                    flTags.visibility = if (flTags.visibility == View.GONE) View.VISIBLE else View.GONE
                    val drawable = if (profileInfo.visibility == View.GONE) R.drawable.ic_down else R.drawable.ic_up
                    val img = ResourcesCompat.getDrawable(resources, drawable, null)
                    tvMoreInformation.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                }
                item.profile.offers?.let {
                    for (tag in it){
                        flTags.addView(SimpleChipView(flTags.context, TagModelImpl(null, tag, null)))
                    }
                }
            }
        }

        private fun getSpannableText(count: String, text: String): SpannableString {
            val value = "$count $text"
            val span = SpannableString(value)
            span.setSpan(ForegroundColorSpan(Color.BLACK), 0, count.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.setSpan(RelativeSizeSpan(1.5f), 0, count.length, 0)
            return span
        }

        private fun setCheckedTags(tvLabel: TextView, flowLayout: FlowLayout, bottomView: View?, tags: List<TagModel>?, text: String) {
            tags?.let {
                tvLabel.text = text
                for (tag in tags) {
                    flowLayout.visibility = View.VISIBLE
                    tvLabel.visibility = View.VISIBLE
                    bottomView?.visibility = View.VISIBLE
                    val chipView = SimpleChipView(flowLayout.context, tag)
                    val params = FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT)
                    chipView.layoutParams = params
                    flowLayout.addView(chipView)
                }
            } ?: kotlin.run {
                flowLayout.visibility = View.GONE
                tvLabel.visibility = View.GONE
                bottomView?.visibility = View.GONE
            }

        }

        private fun setCheckedTexts(tvLabel: TextView, tvText: TextView, bottomView: View?, hint: String, text: String?) {
            text?.let {
                bottomView?.visibility = View.VISIBLE
                tvLabel.visibility = View.VISIBLE
                tvText.visibility = View.VISIBLE
                tvLabel.text = hint
                tvText.text = it
            } ?: run {
                bottomView?.visibility = View.GONE
                tvLabel.visibility = View.GONE
                tvText.visibility = View.GONE
            }
        }

        private fun getDateByTimeMillis(createdAt: Long?): String {
            val cal = Calendar.getInstance()
            cal.timeInMillis = createdAt?: return ""
            return "${DateFormatSymbols().months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
        }

        companion object {
            fun newInstance(parent: ViewGroup, selectedAccount: List<ProfileModel>): ProfileViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_profile_view, parent, false)
                val viewHolder = ProfileViewHolder(selectedAccount, view)
                return viewHolder
            }
        }

    }


}
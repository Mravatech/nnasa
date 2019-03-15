package com.mnassa.screen.profile

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.mnassa.R
import com.mnassa.domain.model.*
import com.mnassa.extensions.formatAsDate
import com.mnassa.extensions.formatted
import com.mnassa.extensions.isGone
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.posts.PostsRVAdapter
import com.mnassa.screen.posts.need.details.adapter.PostTagRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.sub_profile_header.view.*
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Peter on 8/17/2018.
 */
class ProfilePostsRVAdapter(coroutineScope: CoroutineScope, profile: ShortAccountModel) : PostsRVAdapter(coroutineScope = coroutineScope, withHeader = true) {

    var profile: ShortAccountModel = profile
        set(value) {
            field = value
            recyclerView.invoke { notifyItemChanged(0) }
        }
    var offers: List<TagModel>
        get() = offersAdapter.dataStorage.toList()
        set(value) {
            hasOffers = value.isNotEmpty()
            offersAdapter.set(value)
        }
    var interests: List<TagModel>
        get() = interestsAdapter.dataStorage.toList()
        set(value) {
            hasInterests = value.isNotEmpty()
            interestsAdapter.set(value)
        }
    var connectionStatus: ConnectionStatus = ConnectionStatus.NONE
        set(value) {
            field = value
            recyclerView.invoke { notifyItemChanged(0) }
        }
    var onConnectionStatusClick: (ConnectionStatus) -> Unit = { }
    var onConnectionsCountClick: () -> Unit = { }
    var onPointsCountClick: () -> Unit = { }


    private var offersAdapter = PostTagRVAdapter()
    private val interestsAdapter = PostTagRVAdapter()

    private var isAdditionalInfoExpanded: Boolean = false
        set(value) {
            field = value
            recyclerView.invoke { notifyItemChanged(0) }
        }
    private var hasOffers: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                recyclerView.invoke { notifyItemChanged(0) }
            }
        }
    private var hasInterests: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                recyclerView.invoke { notifyItemChanged(0) }
            }
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<PostModel> {
        return if (viewType == TYPE_HEADER) HeaderViewHolder.newInstance(parent, this)
        else super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseVH<PostModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (position == 0) {
            (holder as HeaderViewHolder).bind(Unit)
        }
    }

    class HeaderViewHolder(itemView: View, private val adapter: ProfilePostsRVAdapter) : BasePaginationRVAdapter.BaseVH<Any>(itemView) {

        override fun bind(item: Any) {
            val profile = adapter.profile

            with(itemView) {
                flMoreInformation.isGone = profile !is ProfileAccountModel
                clMore.isGone = !adapter.isAdditionalInfoExpanded
                rvICanHelpHorizontalTags.isGone = adapter.isAdditionalInfoExpanded
                tvMoreInformation.text = if (adapter.isAdditionalInfoExpanded)
                    fromDictionary(R.string.profile_less_information)
                else fromDictionary(R.string.profile_more_information)

                ivMoreInformationIcon.rotation = if (adapter.isAdditionalInfoExpanded) 180.0f else 0.0f

                tvConnectionStatus.text = formatConnectionStatus(adapter.connectionStatus)

                //offers
                tvCanHelpWith.isGone = !adapter.hasOffers
                rvCanHelpWith.isGone = !adapter.hasOffers
                vCanHelpWithDivider.isGone = !adapter.hasOffers

                //interests
                tvInterestsIn.isGone = !adapter.hasInterests
                rvInterestsIn.isGone = !adapter.hasInterests
                vInterestsInDivider.isGone = !adapter.hasInterests

                //mobile phone
                tvMobilePhone.text = profile.contactPhone
                tvMobilePhone.isGone = tvMobilePhone.text.isBlank()
                ivMobilePhone.isGone = tvMobilePhone.isGone
                tvMobilePhoneLabel.isGone = tvMobilePhone.isGone
                vMobilePhoneDivider.isGone = tvMobilePhone.isGone

                //date of birth
                tvDateOfBirthLabel.text =
                        if (profile.accountType == AccountType.PERSONAL) fromDictionary(R.string.profile_date_of_birth)
                        else fromDictionary(R.string.profile_date_of_foundation)

                val counters = profile as? ProfileAccountModel
                //counters
                tvProfileConnections.text = getSpannableText(counters?.numberOfConnections?.toString(), fromDictionary(R.string.profile_connections))
                tvPointsGiven.text = getSpannableText(counters?.visiblePoints?.toString(), fromDictionary(R.string.profile_points_given))

                //full profile section
                if (profile !is ProfileAccountModel) return

                //date of birth
                tvDateOfBirth.text = profile.birthday?.formatAsDate()
                tvDateOfBirth.isGone = tvDateOfBirth.text.isBlank()
                ivDateOfBirth.isGone = tvDateOfBirth.isGone
                tvDateOfBirthLabel.isGone = tvDateOfBirth.isGone
                vDateOfBirthDivider.isGone = tvDateOfBirth.isGone

                //email
                tvEmail.text = profile.contactEmail
                tvEmail.isGone = tvEmail.text.isBlank()
                ivEmail.isGone = tvEmail.isGone
                tvEmailLabel.isGone = tvEmail.isGone
                vEmailDivider.isGone = tvEmail.isGone

                //web site
                tvWebSite.text = profile.website
                tvWebSite.isGone = tvWebSite.text.isBlank()
                ivWebSite.isGone = tvWebSite.isGone
                tvWebSiteDivider.isGone = tvWebSite.isGone
                tvWebSiteLabel.isGone = tvWebSite.isGone

                //location
                tvLocation.text = profile.location?.formatted()
                tvLocation.isGone = profile.location == null
                ivLocation.isGone = tvLocation.isGone
                vLocationDivider.isGone = tvLocation.isGone
            }
        }

        private fun getSpannableText(head: String?, tail: String): SpannableString {
            val head = head ?: ""
            val value = "$head\n$tail"
            val span = SpannableString(value)
            span.setSpan(ForegroundColorSpan(Color.BLACK), 0, head.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.setSpan(RelativeSizeSpan(HeaderViewHolder.PROPORTION_TEXT_SIZE), 0, head.length, 0)
            return span
        }

        private fun formatConnectionStatus(connectionStatus: ConnectionStatus): CharSequence? {
            val text = when (connectionStatus) {
                ConnectionStatus.CONNECTED -> fromDictionary(R.string.user_profile_connection_connected)
                ConnectionStatus.REQUESTED -> fromDictionary(R.string.user_profile_connection_connect)
                ConnectionStatus.RECOMMENDED -> fromDictionary(R.string.user_profile_connection_connect)
                ConnectionStatus.SENT -> fromDictionary(R.string.profile_request_was_sent)
                else -> null
            }
            return getSpannableText("", text ?: "")
        }

        companion object {
            const val PROPORTION_TEXT_SIZE = 1.5f


            fun newInstance(parent: ViewGroup, adapter: ProfilePostsRVAdapter): HeaderViewHolder {
                //footer
                val view = LayoutInflater.from(parent.context).inflate(R.layout.sub_profile_header, parent, false)
                val viewHolder = HeaderViewHolder(view, adapter)

                with(view) {
                    rvICanHelpHorizontalTags.adapter = adapter.offersAdapter
                    rvCanHelpWith.adapter = adapter.offersAdapter
                    rvCanHelpWith.layoutManager = ChipsLayoutManager.newBuilder(context)
                            .setScrollingEnabled(false)
                            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                            .setOrientation(ChipsLayoutManager.HORIZONTAL)
                            .build()
                    rvInterestsIn.adapter = adapter.interestsAdapter
                    rvInterestsIn.layoutManager = ChipsLayoutManager.newBuilder(context)
                            .setScrollingEnabled(false)
                            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                            .setOrientation(ChipsLayoutManager.HORIZONTAL)
                            .build()

                    flMoreInformation.setOnClickListener {
                        adapter.isAdditionalInfoExpanded = !adapter.isAdditionalInfoExpanded
                    }
                    tvProfileConnections.setOnClickListener { adapter.onConnectionsCountClick() }
                    tvPointsGiven.setOnClickListener { adapter.onPointsCountClick() }
                    tvConnectionStatus.tag = viewHolder
                    tvConnectionStatus.setOnClickListener {
                        adapter.onConnectionStatusClick(adapter.connectionStatus)
                    }

                }

                return viewHolder
            }
        }
    }
}
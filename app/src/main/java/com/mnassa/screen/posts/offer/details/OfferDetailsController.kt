package com.mnassa.screen.posts.offer.details

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.domain.model.OfferPostModel
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.*
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.screen.posts.offer.create.CreateOfferController
import com.mnassa.screen.posts.offer.details.buy.BuyOfferController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_need_details.view.*
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/2/2018.
 */
class OfferDetailsController(args: Bundle) : NeedDetailsController(args) {

    override val viewModel: OfferDetailsViewModel by instance(arg = postId)
    private val popupMenuHelper: PopupMenuHelper by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            ivRepost.visibility = View.INVISIBLE
            tvRepostsCount.visibility = View.INVISIBLE
        }
    }


    override suspend fun bindPost(post: PostModel) {
        super.bindPost(post)
        post as OfferPostModel

        with(getViewSuspend()) {
            tvTitle.text = post.title
            tvTitle.goneIfEmpty()

            val offerCategoryString = StringBuilder()

            if (!post.category.isNullOrBlank()) {
                offerCategoryString.append(post.category)

                if (!post.subCategory.isNullOrBlank()) {
                    offerCategoryString.append(" / ")
                    offerCategoryString.append(post.subCategory)
                }
            }
            tvType.text = offerCategoryString
            tvType.goneIfEmpty()

//            bindBuyOfferButton(post) //TODO: uncomment to add ability to buy offer
        }
    }

    override suspend fun showMyPostMenu(view: View, post: PostModel) {
        post as OfferPostModel
        popupMenuHelper.showMyPostMenu(
                view = view,
                post = post,
                onEditPost = { open(CreateOfferController.newInstance(post)) },
                onDeletePost = { viewModel.delete() },
                onPromotePost = { viewModel.promote() })
    }

    private suspend fun bindBuyOfferButton(post: OfferPostModel) {
        with(getViewSuspend()) {
            btnComment.isGone = true
            spacePostAction.isGone = true
            val offerButton = btnRecommend
            offerButton.isGone = false

            when {
                post.isMyPost() -> {
                    offerButton.text = fromDictionary(R.string.offer_button_customers)
                    offerButton.setOnClickListener {
                        //show my customers
                    }
                }
                post.getBoughtItemsCount() == 0 -> {
                    offerButton.text = fromDictionary(R.string.offer_button_buy_first)
                    offerButton.setBackgroundResource(R.drawable.btn_main)
                    offerButton.setOnClickListener {
                        open(BuyOfferController.newInstance(post.author, post.price))
                        //buy first
                    }
                }
                else -> {
                    offerButton.text = fromDictionary(R.string.offer_button_buy_second)
                    offerButton.setBackgroundResource(R.drawable.btn_green)
                    offerButton.setOnClickListener {
                        open(BuyOfferController.newInstance(post.author, post.price))
                        //buy more
                    }
                }
            }
        }
    }

    //action button is always visible
    //TODO: uncomment to add ability to buy offer
//    override suspend fun makePostActionsGone() = makePostActionsVisible()
}
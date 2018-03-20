package com.mnassa.screen.posts.need.details

import android.content.Context
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.salomonbrys.kodein.*
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.registration.RegistrationController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_need_details.view.*
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/19/2018.
 */
class NeedDetailsController(args: Bundle) : MnassaControllerImpl<NeedDetailsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_need_details
    private val postId by lazy { args.getString(EXTRA_NEED_ID) }
    override val viewModel: NeedDetailsViewModel by injector.with(postId).instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {

        }

        launchCoroutineUI {
            viewModel.postChannel.consumeEach {
                setPost(it)
            }
        }
    }

    private fun setPost(post: Post) {
        val view = view ?: return

        with(view) {
            toolbar.title = fromDictionary(R.string.need_details_title).format(post.author.formattedName)

            //author block
            ivAvatar.avatarRound(post.author.avatar)
            tvUserName.text = post.author.formattedName
            tvPosition.text = post.author.formattedPosition
            tvPosition.goneIfEmpty()
            tvEventName.text = post.author.formattedFromEvent
            tvEventName.goneIfEmpty()

            //
            tvNeedDescription.text = post.text
            tvNeedDescription.goneIfEmpty()
            //images
            flImages.visibility = if (post.images.isNotEmpty()) View.VISIBLE else View.GONE
            if (post.images.isNotEmpty()) {
                pivImages.count = post.images.size
                pivImages.selection = 0

                vpImages.adapter = RegistrationAdapter(context, post.images) {
                    Toast.makeText(context, "Open image $it", Toast.LENGTH_SHORT).show()
                }
                vpImages.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        pivImages.selection = position
                    }
                })
            }
            //price
            tvPrice.visibility = if (post.price > 0.0) View.VISIBLE else View.GONE
            tvPrice.text = post.price.formatAsMoney()


            //location
            tvLocation.text = post.locationPlace.formatted()
            //time
            tvCreationTime.text = post.createdAt.toTimeAgo()


        }




    }

    inner class RegistrationAdapter(private val context: Context, private val images: List<String>, private val onClickListener: (String) -> Unit)
        : PagerAdapter() {


        override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageUrl = images[position]
            val view = LayoutInflater.from(container.context).inflate(R.layout.item_image, container, false)
            view.ivImage.image(imageUrl)
            view.setOnClickListener { onClickListener(imageUrl) }
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }


        override fun getCount(): Int = images.size
    }


    companion object {
        private const val EXTRA_NEED_ID = "EXTRA_NEED_ID"
        private const val EXTRA_NEED_MODEL = "EXTRA_NEED_MODEL"

        fun newInstance(postId: String): NeedDetailsController {
            val args = Bundle()
            args.putString(EXTRA_NEED_ID, postId)
            return NeedDetailsController(args)
        }

        fun newInstance(post: Post): NeedDetailsController {
            val args = Bundle()
            args.putString(EXTRA_NEED_ID, post.id)
            args.putSerializable(EXTRA_NEED_MODEL, post)
            return NeedDetailsController(args)
        }

    }
}
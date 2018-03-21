package com.mnassa.screen.posts.need.details

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_need_details.view.*
import kotlinx.android.synthetic.main.controller_need_details_header.view.*
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/19/2018.
 */
class NeedDetailsController(args: Bundle) : MnassaControllerImpl<NeedDetailsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_need_details
    private val postId by lazy { args.getString(EXTRA_NEED_ID) }
    override val viewModel: NeedDetailsViewModel by injector.with(postId).instance()
    private val tagsAdapter = TagRVAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvTags.layoutManager = ChipsLayoutManager.newBuilder(context)
                    //set vertical gravity for all items in a row. Default = Gravity.CENTER_VERTICAL
//                    .setChildGravity(Gravity.TOP)
                    //whether RecyclerView can scroll. TRUE by default
                    .setScrollingEnabled(false)
                    .setRowStrategy(ChipsLayoutManager.STRATEGY_FILL_SPACE)
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()
            rvTags.adapter = tagsAdapter
        }

        launchCoroutineUI {
            viewModel.postChannel.consumeEach {
                setPost(it)
            }
        }

        launchCoroutineUI {
            viewModel.postTagsChannel.consumeEach {
                setTags(it)
            }
        }

        launchCoroutineUI {
            viewModel.finishScreenChannel.consumeEach {
                activity?.onBackPressed()
            }
        }

        (args.getSerializable(EXTRA_NEED_MODEL) as Post?)?.apply { setPost(this) }
    }

    private fun showMyPostMenu(view: View, post: Post) {
        //Creating the instance of PopupMenu
        val popup = PopupMenu(view.context, view)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.post_edit, popup.menu)
        popup.menu.findItem(R.id.action_post_edit).title = fromDictionary(R.string.need_action_edit)
        popup.menu.findItem(R.id.action_post_delete).title = fromDictionary(R.string.need_action_delete)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_post_edit -> open(CreateNeedController.newInstanceEditMode(post))
                R.id.action_post_delete -> viewModel.delete()
            }
            true
        }

        popup.show()
    }

    private fun showPostMenu(view: View) {
        //Creating the instance of PopupMenu
        val popup = PopupMenu(view.context, view)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.post_view, popup.menu)
        popup.menu.findItem(R.id.action_post_repost).title = fromDictionary(R.string.need_action_repost)
        popup.menu.findItem(R.id.action_post_report).title = fromDictionary(R.string.need_action_report)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_post_repost -> Toast.makeText(view.context, "Repost post", Toast.LENGTH_SHORT).show()
                R.id.action_post_report -> Toast.makeText(view.context, "Report post", Toast.LENGTH_SHORT).show()
            }
            true
        }

        popup.show()
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
            ivChat.setOnClickListener {
                Toast.makeText(context, "Opening chat with ${post.author.formattedName}", Toast.LENGTH_SHORT).show()
            }

            //
            tvNeedDescription.text = post.formattedText
            tvNeedDescription.goneIfEmpty()
            //images
            flImages.visibility = if (post.images.isNotEmpty()) View.VISIBLE else View.GONE
            if (post.images.isNotEmpty()) {
                pivImages.count = post.images.size
                pivImages.selection = 0

                vpImages.adapter = RegistrationAdapter(post.images) {
                    PhotoPagerActivity.start(context, post.images, post.images.indexOf(it))
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
            tvLocation.invisibleIfEmpty()
            //time
            tvCreationTime.text = post.createdAt.toTimeAgo()

            //views count
            tvViewsCount.text = fromDictionary(R.string.need_views_count).format(post.counters.views)
            ivRepost.setOnClickListener {
                Toast.makeText(context, "REPOST!", Toast.LENGTH_SHORT).show()
            }
            tvRepostsCount.text = post.counters.reposts.toString()

            btnComment.text = fromDictionary(R.string.need_comment_button)
            btnComment.setOnClickListener {
                Toast.makeText(context, "COMMENT!", Toast.LENGTH_SHORT).show()
            }

            val recommendWithCount = StringBuilder(fromDictionary(R.string.need_recommend_button))
            if (post.autoSuggest.total > 0) {
                recommendWithCount.append(" (")
                recommendWithCount.append(post.autoSuggest.total.toString())
                recommendWithCount.append(") ")
            }

            btnRecommend.text = recommendWithCount

            btnRecommend.setOnClickListener {
                Toast.makeText(context, "RECOMMEND!", Toast.LENGTH_SHORT).show()
            }

            launchCoroutineUI {
                if (post.isMyPost()) {
                    toolbar.onMoreClickListener = { showMyPostMenu(it, post) }
                } else toolbar.onMoreClickListener = { showPostMenu(it) }
            }

        }
    }

    private fun setTags(tags: List<TagModel>) {
        with(view ?: return) {
            vTagsSeparator.visibility = if (tags.isEmpty()) View.GONE else View.VISIBLE
            rvTags.visibility = if (tags.isEmpty()) View.GONE else View.VISIBLE
            tagsAdapter.set(tags)
        }
    }

    class RegistrationAdapter(private val images: List<String>, private val onClickListener: (String) -> Unit) : PagerAdapter() {
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
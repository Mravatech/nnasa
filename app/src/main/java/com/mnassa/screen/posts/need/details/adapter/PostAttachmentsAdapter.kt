package com.mnassa.screen.posts.need.details.adapter

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.domain.model.PostAttachment
import com.mnassa.extensions.image
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.android.synthetic.main.item_video.view.*

/**
 * Created by Peter on 4/30/2018.
 */
class PostAttachmentsAdapter(private val attachments: List<PostAttachment>) : PagerAdapter() {
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val attachment = attachments[position]

        val view = when (attachment) {
            is PostAttachment.PostPhotoAttachment -> bindImage(attachment, container)
            is PostAttachment.PostVideoAttachment -> bindVideo(attachment, container)
        }

        container.addView(view)
        return view
    }

    private fun bindImage(image: PostAttachment.PostPhotoAttachment, container: ViewGroup): View {
        val view = LayoutInflater.from(container.context).inflate(R.layout.item_image, container, false)

        view.ivImage.image(image.photoUrl)
        view.setOnClickListener {
            val allImages = attachments.filterIsInstance<PostAttachment.PostPhotoAttachment>()
            val imageIndex = allImages.indexOf(image)
            if (imageIndex >= 0) {
                PhotoPagerActivity.start(container.context, allImages.map { it.photoUrl }, imageIndex)
            }
        }
        return view
    }

    private fun bindVideo(video: PostAttachment.PostVideoAttachment, container: ViewGroup): View {

        val view = LayoutInflater.from(container.context).inflate(R.layout.item_video, container, false)

        view.ivPreview.image(video.previewUrl)
        view.ivPlay.setOnClickListener {
            TODO()
        }

        return view
    }


    override fun destroyItem(container: ViewGroup, position: Int, view: Any) = container.removeView(view as View)
    override fun getCount(): Int = attachments.size
}
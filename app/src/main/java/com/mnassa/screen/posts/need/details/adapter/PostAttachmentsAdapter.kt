package com.mnassa.screen.posts.need.details.adapter

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.storage.FirebaseStorage
import com.mnassa.R
import com.mnassa.activity.VideoViewActivity
import com.mnassa.core.addons.launchUI
import com.mnassa.data.extensions.await
import com.mnassa.di.getInstance
import com.mnassa.domain.model.PostAttachment
import com.mnassa.extensions.image
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.android.synthetic.main.item_video.view.*
import timber.log.Timber

/**
 * Created by Peter on 4/30/2018.
 */
class PostAttachmentsAdapter(private val attachments: List<PostAttachment>, private val onImageClick: (images: List<String>, index: Int) -> Unit) : PagerAdapter() {
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
                onImageClick(allImages.map { it.photoUrl }, imageIndex)
            }
        }
        return view
    }

    private fun bindVideo(video: PostAttachment.PostVideoAttachment, container: ViewGroup): View {
        val view = LayoutInflater.from(container.context).inflate(R.layout.item_video, container, false)

        view.ivPreview.image(video)
        val listener = View.OnClickListener {
            launchUI {
                try {
                    val storage = it.context.getInstance<FirebaseStorage>()
                    val uri = storage.getReferenceFromUrl(video.videoUrl).downloadUrl.await(it.context.getInstance())
                    VideoViewActivity.start(it.context, uri, video.previewUrl)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

        view.setOnClickListener(listener)
        view.ivPlay.setOnClickListener(listener)

        return view
    }


    override fun destroyItem(container: ViewGroup, position: Int, view: Any) = container.removeView(view as View)
    override fun getCount(): Int = attachments.size
}
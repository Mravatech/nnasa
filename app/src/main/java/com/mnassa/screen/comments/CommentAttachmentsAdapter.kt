package com.mnassa.screen.comments

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.extensions.image
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.posts.need.create.AttachedImage
import kotlinx.android.synthetic.main.item_comment_attached_image.view.*

/**
 * Created by Peter on 6/4/2018.
 */
class CommentAttachmentsAdapter : BasePaginationRVAdapter<AttachedImage>(), View.OnClickListener {

    var onDataSourceChangedListener = { items: List<AttachedImage> -> }

    fun destroyCallbacks() {
        onDataSourceChangedListener = { items: List<AttachedImage> -> }
    }

    init {
        dataStorage = SimpleDataProviderImpl()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<AttachedImage> {
        return ImageAttachmentViewHolder.newInstance(parent, this)
    }

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position >= 0) {
            dataStorage.remove(getDataItemByAdapterPosition(position))
        }
    }

    class ImageAttachmentViewHolder(itemView: View) : BaseVH<AttachedImage>(itemView) {

        override fun bind(item: AttachedImage) {
            with(itemView) {
                if (item is AttachedImage.UploadedImage) {
                    ivImage.image(item.imageUrl)
                } else if (item is AttachedImage.LocalImage) {
                    ivImage.image(item.imageUri)
                }
            }
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): ImageAttachmentViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment_attached_image, parent, false)
                val viewHolder = ImageAttachmentViewHolder(view)

                view.ivRemove.setOnClickListener(onClickListener)
                view.ivRemove.tag = viewHolder

                return viewHolder
            }
        }
    }

}
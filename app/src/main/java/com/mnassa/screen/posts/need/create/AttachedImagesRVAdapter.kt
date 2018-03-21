package com.mnassa.screen.posts.need.create

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.extensions.image
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import kotlinx.android.synthetic.main.item_post_add_image.view.*
import kotlinx.android.synthetic.main.item_post_edit_image.view.*
import java.io.File
import java.io.Serializable

/**
 * Created by Peter on 21.03.2018.
 */
class AttachedImagesRVAdapter : BasePaginationRVAdapter<AttachedImage>(), View.OnClickListener {

    var onAddImageClickListener = {}
    var onRemoveImageClickListener = { image: AttachedImage -> }
    var onReplaceImageClickListener = { image: AttachedImage -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<AttachedImage> {
        return when (viewType) {
            AttachedImage.TYPE_UPLOADED -> UploadedImageVH.newInstance(parent, this)
            AttachedImage.TYPE_ADD -> AddImageVH.newInstance(parent, this)
            AttachedImage.TYPE_LOCAL -> LocalImageVH.newInstance(parent, this)
            else -> throw IllegalArgumentException("Illegal view type: $viewType")
        }
    }

    override fun getViewType(position: Int): Int = dataStorage[position].typeId

    override fun onClick(view: View) {

        val viewHolder = view.tag as RecyclerView.ViewHolder
        val position = viewHolder.adapterPosition
        if (position < 0) return

        when (view.id) {
            R.id.vAddImage -> onAddImageClickListener()
            R.id.btnDelete -> {
                onRemoveImageClickListener(getDataItemByAdapterPosition(position))
            }
            R.id.btnReplace -> {
                onReplaceImageClickListener(getDataItemByAdapterPosition(position))
            }
        }
    }

    class UploadedImageVH(itemView: View) : BaseVH<AttachedImage>(itemView) {

        override fun bind(item: AttachedImage) {
            itemView.ivImage.image((item as AttachedImage.UploadedImage).imageUrl)
        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): UploadedImageVH {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_edit_image, parent, false)
                val viewHolder = UploadedImageVH(view)

                view.btnReplace.setOnClickListener(onClickListener)
                view.btnReplace.tag = viewHolder

                view.btnDelete.setOnClickListener(onClickListener)
                view.btnDelete.tag = viewHolder

                return viewHolder
            }
        }
    }

    class LocalImageVH(itemView: View) : BaseVH<AttachedImage>(itemView) {

        override fun bind(item: AttachedImage) {

        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): LocalImageVH {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_edit_image, parent, false)
                val viewHolder = LocalImageVH(view)

                view.btnReplace.setOnClickListener(onClickListener)
                view.btnReplace.tag = viewHolder

                view.btnDelete.setOnClickListener(onClickListener)
                view.btnDelete.tag = viewHolder

                return viewHolder
            }
        }
    }

    class AddImageVH(itemView: View) : BaseVH<AttachedImage>(itemView) {

        override fun bind(item: AttachedImage) {

        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): AddImageVH {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_add_image, parent, false)
                val viewHolder = AddImageVH(view)
                view.vAddImage.setOnClickListener(onClickListener)
                view.vAddImage.tag = viewHolder
                return viewHolder
            }
        }
    }
}

sealed class AttachedImage(val typeId: Int) : Serializable {
    class UploadedImage(val imageUrl: String) : AttachedImage(TYPE_UPLOADED)
    class LocalImage(val imageFile: File) : AttachedImage(2)
    class AddImage() : AttachedImage(3)

    companion object {
        const val TYPE_UPLOADED = 1
        const val TYPE_LOCAL = 2
        const val TYPE_ADD = 3
    }

}
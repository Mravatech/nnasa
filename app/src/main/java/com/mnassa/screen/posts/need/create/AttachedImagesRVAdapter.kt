package com.mnassa.screen.posts.need.create

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PostAttachment
import com.mnassa.extensions.image
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_post_add_image.view.*
import kotlinx.android.synthetic.main.item_post_edit_image.view.*
import java.io.Serializable

/**
 * Created by Peter on 21.03.2018.
 */
class AttachedImagesRVAdapter : BasePaginationRVAdapter<AttachedImage>(), View.OnClickListener {

    var onAddImageClickListener = {}
    var onRemoveImageClickListener = { position: Int, image: AttachedImage -> }
    var onReplaceImageClickListener = { position: Int, image: AttachedImage -> }

    fun destroyCallbacks() {
        onAddImageClickListener = {}
        onRemoveImageClickListener = { _, _ -> }
        onReplaceImageClickListener = { _, _ -> }
    }

    init {
        dataStorage = object : SimpleDataProviderImpl() {
            override fun clear() {
                set(emptyList())
            }

            override fun add(element: AttachedImage): Boolean {
                set((filter { it.typeId != AttachedImage.TYPE_ADD } + element + AttachedImage.AddImage))
                return true
            }

            override fun addAll(elements: Collection<AttachedImage>): Boolean {
                set((filter { it.typeId != AttachedImage.TYPE_ADD } + elements + AttachedImage.AddImage))
                return true
            }

            override fun set(elements: List<AttachedImage>) {
                if (elements.size >= MAX_PHOTOS_COUNT || elements.contains(AttachedImage.AddImage)) {
                    super.set(elements.take(MAX_PHOTOS_COUNT))
                } else {
                    super.set((elements + AttachedImage.AddImage).take(MAX_PHOTOS_COUNT))
                }
            }

            override fun remove(element: AttachedImage): Boolean {
                val copy = toMutableList()
                val removeResult = copy.remove(element)
                set(copy)
                return removeResult
            }
        }

        dataStorage.set(emptyList())
    }

    fun replace(oldImage: AttachedImage, newImage: AttachedImage) {
        val listCopy = dataStorage.toMutableList()

        val index = listCopy.indexOf(oldImage)
        listCopy[index] = newImage

        set(listCopy)
    }

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
        val position = convertAdapterPositionToDataIndex(viewHolder.adapterPosition)

        if (position < 0) return

        when (view.id) {
            R.id.vAddImage -> onAddImageClickListener()
            R.id.btnDelete -> onRemoveImageClickListener(position, dataStorage[position])
            R.id.btnReplace -> onReplaceImageClickListener(position, dataStorage[position])
        }
    }

    class UploadedImageVH(itemView: View) : BaseVH<AttachedImage>(itemView) {

        override fun bind(item: AttachedImage) {
            itemView.ivImage.image((item as AttachedImage.UploadedImage).imageUrl)
            itemView.btnReplace.text = fromDictionary(R.string.need_create_replace_photo)
            itemView.btnDelete.text = fromDictionary(R.string.need_create_delete_photo)
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
            itemView.ivImage.image((item as AttachedImage.LocalImage).imageUri)
            itemView.btnReplace.text = fromDictionary(R.string.need_create_replace_photo)
            itemView.btnDelete.text = fromDictionary(R.string.need_create_delete_photo)
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

    companion object {
        private const val MAX_PHOTOS_COUNT = 5
    }
}

sealed class AttachedImage(val typeId: Int) : Serializable {
    class UploadedImage(val imageUrl: String) : AttachedImage(TYPE_UPLOADED) {
        constructor(postAttachment: PostAttachment) : this((postAttachment as PostAttachment.PostPhotoAttachment).photoUrl)
    }
    class LocalImage(val imageUri: Uri) : AttachedImage(TYPE_LOCAL)
    object AddImage : AttachedImage(TYPE_ADD)

    companion object {
        const val TYPE_UPLOADED = 1
        const val TYPE_LOCAL = 2
        const val TYPE_ADD = 3
    }
}
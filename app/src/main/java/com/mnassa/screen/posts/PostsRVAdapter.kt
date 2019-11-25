package com.mnassa.screen.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.core.addons.launchUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.isRepost
import com.mnassa.extensions.startUpdateTimeJob
import com.mnassa.extensions.stopUpdateTimeJob
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import com.mnassa.screen.posts.viewholder.*
import kotlinx.android.synthetic.main.header_main.*
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Peter on 3/14/2018.
 */
open class PostsRVAdapter(private val coroutineScope: CoroutineScope, private val withHeader: Boolean = true) : BaseSortedPaginationRVAdapter<PostModel>(), View.OnClickListener {
    var onAttachedToWindow: (item: PostModel) -> Unit = {}
    var onDetachedFromWindow: (item: PostModel) -> Unit = {}
    var onItemClickListener = { item: PostModel -> }
    var onCreateNeedClickListener = {}
    var onRepostedByClickListener = { account: ShortAccountModel -> }
    var onPostedByClickListener = { account: ShortAccountModel -> }
    var onHideInfoPostClickListener = { post: PostModel -> }
    var onGroupClickListener = { group: GroupModel -> }
    var onMoreItemClickListener = { item: PostModel, view: View -> }
    var onRepostClickListener = { item: PostModel -> }
    var onOffersClickListener = { account: PostModel, account1: ShortAccountModel -> }
    var onRecommendationClickListener = { item: PostModel -> }



    var showMoreOptions: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun destroyCallbacks() {
        onAttachedToWindow = {}
        onDetachedFromWindow = {}
        onItemClickListener = {}
        onCreateNeedClickListener = {}
        onRepostedByClickListener = {}
        onPostedByClickListener = {}
        onHideInfoPostClickListener = { }
        onGroupClickListener = { }
        onMoreItemClickListener = { item: PostModel, view: View -> }
    }

    override val itemsComparator: (item1: PostModel, item2: PostModel) -> Int = { first, second ->
        val firstPinned = (first as? InfoPostModel)?.isPinned ?: false
        val secondPinned = (second as? InfoPostModel)?.isPinned ?: false
        when {
            firstPinned && !secondPinned -> -1
            secondPinned && !firstPinned -> 1
            else -> first.createdAt.compareTo(second.createdAt) * -1
        }
    }
    override val itemClass: Class<PostModel> = PostModel::class.java

    init {
        itemsTheSameComparator = { first, second -> first.id == second.id }
        contentTheSameComparator = { first, second ->
            first.counters == second.counters &&
                    first.updatedAt == second.updatedAt &&
                    first.attachments == second.attachments &&
                    first.locationPlace == second.locationPlace &&
                    first.privacyType == second.privacyType &&
                    first.tags == second.tags &&
                    first.text == second.text &&
                    first.statusOfExpiration == second.statusOfExpiration &&
                    first.price == second.price &&
                    first.autoSuggest == second.autoSuggest &&
                    (first as? InfoPostModel)?.isPinned ?: false == (second as? InfoPostModel)?.isPinned ?: false
        }
        dataStorage = SortedDataStorage(itemClass, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<PostModel> {
        return if (viewType == TYPE_HEADER && withHeader) HeaderViewHolder.newInstance(parent, this) else super.onCreateViewHolder(parent, viewType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<PostModel> {
        return when {
            viewType.hasFlag(NEED) || viewType.hasFlag(GENERAL) -> NeedViewHolder.newInstance(
                    parent = parent,
                    onClickListener = this,
                    imagesCount = viewType.getImagesCount(),
                    isRepost = viewType.hasFlag(REPOST),
                    isPromoted = viewType.hasFlag(PROMOTED),
                    fromGroup = viewType.hasFlag(FROM_GROUP),
                    hasOptions = viewType.hasFlag(HAS_OPTIONS)
            )
            viewType.hasFlag(OFFER) -> OfferViewHolder.newInstance(
                    parent = parent,
                    imagesCount = viewType.getImagesCount(),
                    onClickListener = this,
                    isPromoted = viewType.hasFlag(PROMOTED),
                    fromGroup = viewType.hasFlag(FROM_GROUP),
                    hasOptions = viewType.hasFlag(HAS_OPTIONS)
            )
            viewType.hasFlag(PROFILE) -> ProfileViewHolder.newInstance(
                    parent = parent,
                    onClickListener = this,
                    isRepost = viewType.hasFlag(REPOST),
                    isPromoted = viewType.hasFlag(PROMOTED),
                    fromGroup = viewType.hasFlag(FROM_GROUP),
                    hasOptions = viewType.hasFlag(HAS_OPTIONS)
            )
            viewType.hasFlag(INFO) -> InfoViewHolder.newInstance(
                    parent = parent,
                    onClickListener = this,
                    imagesCount = viewType.getImagesCount(),
                    isPinned = viewType.hasFlag(PINNED)
            )
            viewType.hasFlag(OTHER) -> UnsupportedTypeViewHolder.newInstance(
                    parent = parent,
                    onClickListener = this
            )
            else -> throw IllegalStateException("Illegal view type $viewType")
        }
    }

    override fun onViewAttachedToWindow(holder: BaseVH<PostModel>) {
        super.onViewAttachedToWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition).takeIf { it >= 0 }
                ?: return
        val dataItem = dataStorage[position]
        onAttachedToWindow(dataItem)

        val postModel = getDataItemByAdapterPosition(holder.adapterPosition)

        val tvRepostCounttv =  holder.itemView.findViewById<TextView>(R.id.tvRepostCount)

        tvRepostCounttv.text = "${postModel.counters.reposts}"
        tvRepostCounttv.setOnClickListener {

            onRepostClickListener(postModel)
        }


        holder.itemView.findViewById<TextView>(R.id.tvOffersCount)?.setOnClickListener {
            onOffersClickListener(getDataItemByAdapterPosition(holder.adapterPosition), getDataItemByAdapterPosition(holder.adapterPosition).author )
        }

        holder.itemView.findViewById<TextView>(R.id.tvRecomendationsCount)?.setOnClickListener {

            onRecommendationClickListener(getDataItemByAdapterPosition(holder.adapterPosition))
        }



        holder.itemView.findViewById<TextView?>(R.id.tvTime)?.let {
            coroutineScope.launchUI {
                it.startUpdateTimeJob(dataItem.originalCreatedAt)
            }
        }

        holder.itemView.findViewById<TextView?>(R.id.tvReplyTime)?.let {
            coroutineScope.launchUI {
                it.startUpdateTimeJob(dataItem.createdAt)
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseVH<PostModel>) {
        super.onViewDetachedFromWindow(holder)

        val position = convertAdapterPositionToDataIndex(holder.adapterPosition).takeIf { it >= 0 }
                ?: return
        val dataItem = dataStorage[position]
        onDetachedFromWindow(dataItem)

        holder.itemView.findViewById<TextView?>(R.id.tvTime)?.let {
            it.stopUpdateTimeJob()
        }
        holder.itemView.findViewById<TextView?>(R.id.tvReplyTime)?.let {
            it.stopUpdateTimeJob()
        }
    }

    override fun getViewType(position: Int): Int {
        val item = dataStorage[position]

        var type = 0
        type = type or when (item.type) {
            is PostType.NEED -> NEED
            is PostType.OFFER -> OFFER
            is PostType.GENERAL -> GENERAL
            is PostType.PROFILE -> PROFILE
            is PostType.INFO -> INFO
            is PostType.OTHER -> OTHER
        }

        if (item.isRepost) {
            type = type or REPOST
        }

        type = type or when (item.attachments.size) {
            0 -> 0
            1 -> IMAGE_1
            2 -> IMAGE_2
            3 -> IMAGE_3
            else -> IMAGE_MORE
        }

        if (item is InfoPostModel && item.isPinned) {
            type = type or PINNED
        }

        if (item.privacyType is PostPrivacyType.WORLD) {
            type = type or PROMOTED
        }

        if (item.groups.isNotEmpty()) {
            type = type or FROM_GROUP
        }

        if (showMoreOptions) {
            type = type or HAS_OPTIONS
        }

        return type
    }

    override fun onClick(view: View) {
        val tag = view.tag
        if (tag is GroupModel) {
            onGroupClickListener(tag)
            return
        }

        val position = (tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.rlClickableRoot -> onItemClickListener(getDataItemByAdapterPosition(position))
            R.id.flCreateNeed -> onCreateNeedClickListener()
            R.id.rlRepostRoot -> onRepostedByClickListener(requireNotNull(getDataItemByAdapterPosition(position).repostAuthor))
            R.id.rlAuthorRoot -> onPostedByClickListener(getDataItemByAdapterPosition(position).author)
            R.id.btnHidePost -> onHideInfoPostClickListener(getDataItemByAdapterPosition(position))
            R.id.btnMoreOptions -> onMoreItemClickListener(getDataItemByAdapterPosition(position), view)
            R.id.tvRepostCount -> onRepostClickListener(getDataItemByAdapterPosition(position))
        }
    }

    private companion object {
        private const val NEED = 1 shl 1
        private const val OFFER = 1 shl 2
        private const val INFO = 1 shl 3
        private const val PROFILE = 1 shl 4
        private const val GENERAL = 1 shl 5
        private const val OTHER = 1 shl 6

        private const val IMAGE_1 = 1 shl 7
        private const val IMAGE_2 = 1 shl 8
        private const val IMAGE_3 = 1 shl 9
        private const val IMAGE_MORE = 1 shl 10

        private const val REPOST = 1 shl 11
        private const val PINNED = 1 shl 12
        private const val PROMOTED = 1 shl 13
        private const val FROM_GROUP = 1 shl 14
        private const val HAS_OPTIONS = 1 shl 15

        private fun Int.hasFlag(flag: Int) = this and flag == flag

        private fun Int.getImagesCount() = when {
            hasFlag(IMAGE_1) -> 1
            hasFlag(IMAGE_2) -> 2
            hasFlag(IMAGE_3) -> 3
            hasFlag(IMAGE_MORE) -> 4
            else -> 0
        }
    }
}
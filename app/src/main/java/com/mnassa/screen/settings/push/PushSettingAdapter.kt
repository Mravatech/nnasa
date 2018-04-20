package com.mnassa.screen.settings.push

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.PushSettingModel
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.base.adapter.BaseSortedPaginationRVAdapter
import kotlinx.android.synthetic.main.item_push_settings.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */

class PushSettingAdapter : BaseSortedPaginationRVAdapter<PushSettingModel>(), View.OnClickListener {
    override val itemsComparator: (item1: PushSettingModel, item2: PushSettingModel) -> Int = { first, second ->
        first.name.compareTo(second.name)
    }

    override val itemClass: Class<PushSettingModel> = PushSettingModel::class.java

    var onSettingVolumeClick = { item: PushSettingModel -> }
    var onSettingReceiveClick = { item: PushSettingModel -> }

    init {
        itemsTheSameComparator = { first, second -> first.name == second.name }
        contentTheSameComparator = { first, second ->
            first == second
        }
        dataStorage = PushSettingDataStorage(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BaseVH<PushSettingModel> =
            PushSettingHolder.newInstance(parent, this)

    override fun onClick(view: View) {
        val position = (view.tag as RecyclerView.ViewHolder).adapterPosition
        if (position < 0) return
        when (view.id) {
            R.id.swthPush -> onSettingReceiveClick(getDataItemByAdapterPosition(position))
            R.id.ivPushSound -> onSettingVolumeClick(getDataItemByAdapterPosition(position))
        }

    }

    class PushSettingHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<PushSettingModel>(itemView) {
        override fun bind(item: PushSettingModel) {
            with(itemView) {
                swthPush.isChecked = item.isActive
                val imageVolume = if (item.withSound) R.drawable.ic_volume else R.drawable.ic_mute
                ivPushSound.setImageResource(imageVolume)
                tvPushName.text = item.name
                ivPushSound.setOnClickListener(onClickListener)
                ivPushSound.tag = this@PushSettingHolder
                swthPush.setOnClickListener(onClickListener)
                swthPush.tag = this@PushSettingHolder
            }

        }

        companion object {
            fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): PushSettingHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_push_settings, parent, false)
                return PushSettingHolder(view, onClickListener)
            }
        }

    }

    class PushSettingDataStorage(private val adapter: BaseSortedPaginationRVAdapter<PushSettingModel>) :
            SortedDataStorage<PushSettingModel>(PushSettingModel::class.java, adapter), DataStorage<PushSettingModel> {

        override fun addAll(elements: Collection<PushSettingModel>): Boolean {
            adapter.postUpdate {
                wrappedList.beginBatchedUpdates()
                wrappedList.addAll(elements)//todo fix
                wrappedList.endBatchedUpdates()
            }
            return true
        }


    }


}
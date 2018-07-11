package com.mnassa.widget.linearchip

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import com.mnassa.R
import com.mnassa.domain.model.TagModel
import com.mnassa.widget.SimpleChipView

@Deprecated("Use RecyclerView with PostTagRVAdapter instead!")
class OneLineLinear : LinearLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    lateinit var btnCount: CountChipView

    fun setTags(tags: List<TagModel>) {
        val vto = viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                var width = measuredWidth
                var count = START_COUNT_TO_DISPLAY
                val marginsWidth = +resources.getDimension(R.dimen.count_chip_view_inner_margin) * HALF_VIEW + resources.getDimension(R.dimen.count_chip_view_margin) * HALF_VIEW
                for (tag in tags) {
                    val tagView = SimpleChipView(context, tag)
                    if (width > MIN_SPACE_ON_SCREEN_TO_ADD_VIEW) {
                        addView(tagView)
                        tagView.measure(0, 0)
                        val viewWidth = tagView.measuredWidth
                        width -= viewWidth + marginsWidth.toInt()
                        if (width + marginsWidth < MIN_SPACE_ON_SCREEN_TO_ADD_VIEW) {
                            removeView(tagView)
                            btnCount = CountChipView(context)
                            addView(btnCount)
                            break
                        }
                        count++
                    } else {
                        btnCount = CountChipView(context)
                        addView(btnCount)
                        break
                    }
                }
                val left = tags.size - count
                if (left == END_COUNT_TO_DISPLAY && ::btnCount.isInitialized) {
                    btnCount.visibility = View.GONE
                    return
                } else if (::btnCount.isInitialized) {
                    if (left == 1) {
                        val tagView = SimpleChipView(context, tags[tags.size - 1])
                        addView(tagView)
                        btnCount.visibility = View.GONE
                        return
                    }
                    btnCount.setText("+$left")
                }
            }
        })
    }

    companion object {
        const val HALF_VIEW = 2
        const val MIN_SPACE_ON_SCREEN_TO_ADD_VIEW = 0
        const val START_COUNT_TO_DISPLAY = 0
        const val END_COUNT_TO_DISPLAY = 0
    }

}
package com.sk4atg89.alexander.tattag

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import com.mnassa.R
import com.mnassa.domain.model.TagModel
import com.mnassa.widget.SimpleChipView
import com.mnassa.widget.linearchip.CountChipView

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
                var count = 0
                val marginsWidth = +resources.getDimension(R.dimen.count_chip_view_inner_margin) * 2 + resources.getDimension(R.dimen.count_chip_view_margin) * 2
                for (tag in tags) {
                    val tagView = SimpleChipView(context, tag)
                    if (width > 0) {
                        addView(tagView)
                        tagView.measure(0, 0)
                        val viewWidth = tagView.measuredWidth
                        width -= viewWidth + marginsWidth.toInt()
                        if (width + marginsWidth < 0) {
                            removeView(tagView)
                            btnCount = CountChipView(context)
                            btnCount.layoutParams =
                                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT)
                            addView(btnCount)
                            break
                        }
                        count++
                    }else{
                        btnCount = CountChipView(context)
                        btnCount.layoutParams =
                                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT)
                        addView(btnCount)
                        break
                    }
                }
                val left = tags.size - count
                if (left == 0 && ::btnCount.isInitialized) {
                    btnCount.visibility = View.GONE
                    return
                } else if (::btnCount.isInitialized) {
                    btnCount.setText("+$left")
                }

            }
        })

    }

}
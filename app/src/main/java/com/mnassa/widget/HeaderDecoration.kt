package com.mnassa.widget

/**
 * Created by Peter on 3/15/2018.
 */

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View

class HeaderDecoration(context: Context, parent: RecyclerView, @LayoutRes resId: Int) : RecyclerView.ItemDecoration() {

    val headerLayout: View = LayoutInflater.from(context).inflate(resId, parent, false)

    init {
        // inflate and measure the layout
        headerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        // layout basically just gets drawn on the reserved space on top of the first view
        headerLayout.layout(parent.left, 0, parent.right, headerLayout.measuredHeight)
        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            if (parent.getChildAdapterPosition(view) == 0) {
                c.save()
                val height = headerLayout.measuredHeight
                val top = view.top - height
                c.translate(0f, top.toFloat())
                headerLayout.draw(c)
                c.restore()
                break
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.set(0, headerLayout.measuredHeight, 0, 0)
        } else {
            outRect.setEmpty()
        }
    }
}
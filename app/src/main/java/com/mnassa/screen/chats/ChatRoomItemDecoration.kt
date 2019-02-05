package com.mnassa.screen.chats

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R


/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
class ChatRoomItemDecoration(private var divider: Drawable) : RecyclerView.ItemDecoration() {


    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        canvas.save()
        val leftWithMargin = parent.resources.getDimension(R.dimen.chat_bottom_line_padding_start)
        val right = parent.width
        val childCount = parent.childCount
        for (i in 0 until childCount - LAST_CHILD_NOT_TO_DRAW) {
            val child = parent.getChildAt(i)
            val bottom = child.bottom + Math.round(ViewCompat.getTranslationY(child))
            val top = bottom - divider.getIntrinsicHeight()
            divider.setBounds(leftWithMargin.toInt(), top, right, bottom)
            divider.draw(canvas)
        }
        canvas.restore()
    }

    companion object {
        const val LAST_CHILD_NOT_TO_DRAW = 2
    }
}
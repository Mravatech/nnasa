package com.mnassa.widget.coins

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/17/2018
 */

class Coin(startPoint: PointF, private val paint: Paint, val direction: Direction) {


    var drawPoint = PointF(startPoint.x, startPoint.y)

    fun draw(canvas: Canvas) {
        draw(canvas, drawPoint)
    }

    private fun draw(canvas: Canvas, drawPoint: PointF) {
        canvas.drawCircle(drawPoint.x, drawPoint.y, SIZE, paint)
    }

    companion object {
        private var SIZE = 20f
        fun generate(point: PointF, paint: Paint, direction: Direction): Coin {
            return Coin(point, paint, direction)
        }
    }

    enum class Direction {
        LEFT_TO_RIGHT, LEFT_TO_RIGHT_DOWN, LEFT_TO_RIGHT_UP, RIGHT_TO_LEFT, RIGHT_TO_LEFT_DOWN, RIGHT_TO_LEFT_UP,
    }
}
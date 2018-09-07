package com.mnassa.widget.coins

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF

class Coin(startPoint: PointF, private val paint: Paint, private val radius: Float, val direction: Direction) {


    var drawPoint = PointF(startPoint.x, startPoint.y)

    fun draw(canvas: Canvas) {
        draw(canvas, drawPoint)
    }

    private fun draw(canvas: Canvas, drawPoint: PointF) {
        canvas.drawCircle(drawPoint.x, drawPoint.y, radius, paint)
    }

    companion object {
        fun generate(point: PointF, paint: Paint, radius: Float, direction: Direction): Coin {
            return Coin(point, paint, radius, direction)
        }
    }

    enum class Direction {
        LEFT_TO_RIGHT,
        LEFT_TO_RIGHT_DOWN,
        LEFT_TO_RIGHT_UP,
        RIGHT_TO_LEFT,
        RIGHT_TO_LEFT_DOWN,
        RIGHT_TO_LEFT_UP,
    }
}
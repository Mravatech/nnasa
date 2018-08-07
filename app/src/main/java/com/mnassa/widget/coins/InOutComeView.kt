package com.mnassa.widget.coins

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import com.mnassa.R
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.in_out_come_view.view.*
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/17/2018
 */

class InOutComeView : FrameLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val viewRefreshAnimator = ValueAnimator.ofInt(0, 1)
    private val random = Random()
    private var coins = mutableListOf<Coin>()
    private val paint = Paint()
    private var gravity = 0f

    init {
        View.inflate(context, R.layout.in_out_come_view, this)
        setWillNotDraw(false)
        viewRefreshAnimator.duration = ANIMATION_SLIDE_UP_DELAY
        viewRefreshAnimator.addUpdateListener {
            gravity += random.nextFloat() * (MAX - MIN)
            coins.forEach {
                val x = it.drawPoint.x
                val y = it.drawPoint.y
                when (it.direction) {
                    Coin.Direction.LEFT_TO_RIGHT -> {
                        it.drawPoint.y = y + gravity
                        it.drawPoint.x = x - HORIZONTAL_ADD_GRAVITY_X
                    }
                    Coin.Direction.LEFT_TO_RIGHT_DOWN -> {
                        it.drawPoint.y = y + gravity
                        it.drawPoint.x = x + DOWN_ADD_GRAVITY_X
                    }
                    Coin.Direction.LEFT_TO_RIGHT_UP -> {
                        it.drawPoint.y = y - UP_ADD_GRAVITY_Y
                        it.drawPoint.x = x - UP_ADD_GRAVITY_X
                    }
                    Coin.Direction.RIGHT_TO_LEFT -> {
                        it.drawPoint.y = y + gravity
                        it.drawPoint.x = x + HORIZONTAL_ADD_GRAVITY_X
                    }
                    Coin.Direction.RIGHT_TO_LEFT_DOWN -> {
                        it.drawPoint.y = y + gravity
                        it.drawPoint.x = x - DOWN_ADD_GRAVITY_X
                    }
                    Coin.Direction.RIGHT_TO_LEFT_UP -> {
                        it.drawPoint.y = y - UP_ADD_GRAVITY_Y
                        it.drawPoint.x = x + UP_ADD_GRAVITY_X
                    }
                }
            }
            invalidate()
        }
    }


    fun showView(count: Long, name: String?) {
        val from = StringBuilder()//"You just receive 50 points from ${name?:"Admin"}"
        paint.isAntiAlias = true
        if (count < 0) {
            paint.color = ContextCompat.getColor(context, R.color.money_spent)
            btnInOutCome.setBackgroundResource(R.drawable.out_come_btn_background)
            btnInOutCome.text = "$count"
            from.append(fromDictionary(R.string.notifications_spent).format(count))
        } else {
            paint.color = ContextCompat.getColor(context, R.color.money_gained)
            btnInOutCome.setBackgroundResource(R.drawable.in_come_btn_background)
            btnInOutCome.text = "+$count"
            from.append(fromDictionary(R.string.notifications_receive).format(count))
            name?.let {
                from.append(" from ")
                from.append(it)
            }
        }
        gravity = GRAVITY_BEGIN
        tvInOutCome.text = from.toString()
        showWithAnimation(tvInOutCome, ANIMATION_TEXT_DELAY, false)
        showWithAnimation(btnInOutCome, ANIMATION_BTN_DELAY, true)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        coins.forEach {
            it.draw(canvas)
        }
    }

    private fun showWithAnimation(view: View, startDelay: Long, isButton: Boolean) {
        view.animate()
                .translationY(150f)
                .setInterpolator(OvershootInterpolator())
                .setDuration(ANIMATION_SLIDE_DOWN_DURATION)
                .setStartDelay(startDelay)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        view.animate()
                                .translationY(START_PLACE)
                                .setInterpolator(AnticipateOvershootInterpolator())
                                .setStartDelay(ANIMATION_SLIDE_UP_DELAY)
                                .setDuration(ANIMATION_SLIDE_DOWN_DURATION)
                                .setListener(null)
                        if (isButton) {
                            coins.clear()
                            val x = btnInOutCome.x
                            val y = btnInOutCome.y
                            val width = btnInOutCome.width
                            val height = btnInOutCome.height

                            for (i in 1..COINS_COUNT) {
                                val coin = Coin.generate(
                                        paint = paint,
                                        point = PointF(
                                                x + random.nextInt(maxOf(width, 1)),
                                                y + random.nextInt(maxOf(height, 1))),
                                        direction = Coin.Direction.values()[i % DIRECTION_WAYS]
                                )
                                coins.add(coin)
                            }
                            viewRefreshAnimator.start()
                        }
                    }

                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                })
    }

    companion object {
        const val COINS_COUNT = 18
        const val DIRECTION_WAYS = 6
        const val ANIMATION_SLIDE_DOWN_DURATION = 500L
        const val ANIMATION_SLIDE_UP_DELAY = 3000L
        const val ANIMATION_BTN_DELAY = 100L
        const val ANIMATION_TEXT_DELAY = 10L
        const val START_PLACE = 0f
        const val GRAVITY_BEGIN = -10f
        const val MAX = 1.2f
        const val MIN = .3f
        const val HORIZONTAL_ADD_GRAVITY_X = 5
        const val UP_ADD_GRAVITY_X = 2
        const val UP_ADD_GRAVITY_Y = 10
        const val DOWN_ADD_GRAVITY_X = 10
    }

}
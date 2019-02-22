package com.mnassa.widget.coins

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.mnassa.R
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.in_out_come_view.view.*
import java.util.*

class InOutComeView : FrameLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val viewRefreshAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val random = Random()
    private var coins = mutableListOf<Coin>()
    private val paint = Paint()
    private var gravity = 0f

    init {
        val gravityHorizontalX = resources.getDimensionPixelSize(R.dimen.points_animation_gravity_horizontal_x)
        val gravityVerticalUpX = resources.getDimensionPixelSize(R.dimen.points_animation_gravity_up_x)
        val gravityVerticalUpY = resources.getDimensionPixelSize(R.dimen.points_animation_gravity_up_y)
        val gravityVerticalDownX = resources.getDimensionPixelSize(R.dimen.points_animation_gravity_down_x)
        val gravityCoefficient = resources.getDimensionPixelSize(R.dimen.points_animation_gravity)

        View.inflate(context, R.layout.in_out_come_view, this)
        setWillNotDraw(false)
        viewRefreshAnimator.duration = ANIMATION_SLIDE_UP_DELAY
        viewRefreshAnimator.addUpdateListener {
            gravity += random.nextFloat() * gravityCoefficient
            coins.forEach {
                val x = it.drawPoint.x
                val y = it.drawPoint.y

                when (it.direction) {
                    Coin.Direction.LEFT_TO_RIGHT -> {
                        it.drawPoint.y = y + gravity
                        it.drawPoint.x = x - gravityHorizontalX
                    }
                    Coin.Direction.LEFT_TO_RIGHT_DOWN -> {
                        it.drawPoint.y = y + gravity
                        it.drawPoint.x = x + gravityVerticalDownX
                    }
                    Coin.Direction.LEFT_TO_RIGHT_UP -> {
                        it.drawPoint.y = y - gravityVerticalUpY
                        it.drawPoint.x = x - gravityVerticalUpX
                    }
                    Coin.Direction.RIGHT_TO_LEFT -> {
                        it.drawPoint.y = y + gravity
                        it.drawPoint.x = x + gravityHorizontalX
                    }
                    Coin.Direction.RIGHT_TO_LEFT_DOWN -> {
                        it.drawPoint.y = y + gravity
                        it.drawPoint.x = x - gravityVerticalDownX
                    }
                    Coin.Direction.RIGHT_TO_LEFT_UP -> {
                        it.drawPoint.y = y - gravityVerticalUpY
                        it.drawPoint.x = x + gravityVerticalUpX
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
        gravity = resources.getDimensionPixelSize(R.dimen.points_animation_gravity_begin).toFloat()
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
                .translationY(resources.getDimensionPixelOffset(R.dimen.points_animation_top_offset).toFloat())
                .setInterpolator(OvershootInterpolator())
                .setDuration(ANIMATION_SLIDE_DOWN_DURATION)
                .setStartDelay(startDelay)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
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
                                        radius = resources.getDimensionPixelSize(R.dimen.points_animation_coin_size).toFloat(),
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
    }

}
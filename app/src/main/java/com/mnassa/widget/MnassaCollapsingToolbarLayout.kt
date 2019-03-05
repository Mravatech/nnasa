package com.mnassa.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * @author Artem Chepurnoy
 */
class MnassaCollapsingToolbarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CollapsingToolbarLayout(context, attrs, defStyleAttr) {

    private val onOffsetChangedListener =
        AppBarLayout.OnOffsetChangedListener { appBarLayout, offset ->
            val appBarHeight = appBarLayout.measuredHeight
            val dy = appBarHeight.toFloat() - scrimVisibleHeightTrigger

            val scrimAlphaLinearInverted = (dy + offset) / dy
            val scrimAlphaLinear = MAX_COLOR_CHANNEL_FLOAT - scrimAlphaLinearInverted
            scrimAlpha = scrimAlphaLinear.pow(2)
        }

    private var scrimAlpha: Float = 0f
        set(value) {
            field = min(max(value, MIN_COLOR_CHANNEL_FLOAT), MAX_COLOR_CHANNEL_FLOAT)

            // Convert to integer representation and
            // update the drawable.
            val alpha = (field * MAX_COLOR_CHANNEL_INT).toInt()
            setScrimAlpha(alpha)
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        parent.runAs<AppBarLayout> {
            addOnOffsetChangedListener(onOffsetChangedListener)
        }
    }

    override fun onDetachedFromWindow() {
        parent.runAs<AppBarLayout> {
            removeOnOffsetChangedListener(onOffsetChangedListener)
        }

        super.onDetachedFromWindow()
    }

    private inline fun <reified T> Any.runAs(crossinline block: T.() -> Unit) =
        let { it as? T }?.let(block)

    override fun setScrimsShown(shown: Boolean, animate: Boolean) {
        // ignored
    }

    companion object {
        private const val MAX_COLOR_CHANNEL_INT = 255
        private const val MAX_COLOR_CHANNEL_FLOAT = 1f
        private const val MIN_COLOR_CHANNEL_FLOAT = 0f
    }

}

fun CollapsingToolbarLayout.setScrimAlpha(alpha: Int) {
    val method =
        CollapsingToolbarLayout::class.java.getDeclaredMethod("setScrimAlpha", Int::class.java)
            .apply {
                isAccessible = true
            }

    method.invoke(this, alpha)
}

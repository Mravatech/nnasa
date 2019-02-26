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

            scrimAlpha = (1f - (dy + offset) / dy).pow(2)
        }

    private var scrimAlpha: Float = 0f
        set(value) {
            field = min(max(value, 0f), 1f)

            // Convert to integer representation and
            // update the drawable.
            val alpha = (field * 255).toInt()
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

}

fun CollapsingToolbarLayout.setScrimAlpha(alpha: Int) {
    val method =
        CollapsingToolbarLayout::class.java.getDeclaredMethod("setScrimAlpha", Int::class.java)
            .apply {
                isAccessible = true
            }

    method.invoke(this, alpha)
}

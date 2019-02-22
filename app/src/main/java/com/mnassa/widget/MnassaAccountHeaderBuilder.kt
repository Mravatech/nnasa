package com.mnassa.widget

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.ImageViewCompat
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mnassa.extensions.avatarSquare

/**
 * @author Artem Chepurnoy
 */
class MnassaAccountHeaderBuilder : AccountHeaderBuilder() {

    private lateinit var backgroundImageView: ImageView

    private lateinit var backgroundFrameLayout: FrameLayout

    override fun build(): AccountHeader {
        backgroundImageView = ImageView(mActivity).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        backgroundFrameLayout = FrameLayout(mActivity).apply {
            id = View.generateViewId()

            addView(
                backgroundImageView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            // Add tint view
            addView(
                View(mActivity).apply {
                    background = ColorDrawable(0x44000000)
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        return super.build()!!.apply {
            // Insert the profile background image view, this
            // way we won't need to edit the whole layout.
            val root = view as ConstraintLayout
            val lp = ViewGroup.LayoutParams(0, 0)
            root.addView(backgroundFrameLayout, 0, lp)

            // Apply the constraints
            ConstraintSet().apply {
                clone(root)
                connect(backgroundFrameLayout.id, ConstraintSet.START, root.id, ConstraintSet.START)
                connect(backgroundFrameLayout.id, ConstraintSet.TOP, root.id, ConstraintSet.TOP)
                connect(backgroundFrameLayout.id, ConstraintSet.END, root.id, ConstraintSet.END)
                connect(
                    backgroundFrameLayout.id,
                    ConstraintSet.BOTTOM,
                    root.id,
                    ConstraintSet.BOTTOM
                )

                applyTo(root)
            }

            // Fix colors
            mCurrentProfileName.setTextColor(Color.WHITE)
            mCurrentProfileEmail.setTextColor(Color.LTGRAY)
            ImageViewCompat.setImageTintList(
                mAccountSwitcherArrow,
                ColorStateList.valueOf(Color.WHITE)
            )
        }
    }

    override fun buildProfiles() {
        super.buildProfiles()

        if (::backgroundImageView.isInitialized) {
            val profile = mCurrentProfile as? MnassaProfileDrawerItem?
            backgroundImageView.avatarSquare(profile?.account?.avatar)
        }
    }

}
package com.mnassa.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.mnassa.R
import com.mnassa.extensions.image
import com.rd.PageIndicatorView
import kotlinx.android.synthetic.main.activity_photo_pager.*
import kotlinx.android.synthetic.main.item_image.view.*

/**
 * Created by Peter on 20.03.2018.
 */
class PhotoPagerActivity : AppCompatActivity(), View.OnClickListener {
    private var decorView: View? = null
    private var isNavigationVisible = false

    private val images: List<String> by lazy { intent.getSerializableExtra(EXTRA_IMAGES) as List<String> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_pager)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        decorView = window.decorView

        pager.adapter = RegistrationAdapter(images, this)

        pivImages.count = images.size
        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                pivImages.selection = position
            }
        })
        if (savedInstanceState == null) {
            pager.currentItem = intent.getIntExtra(EXTRA_SELECTED_ITEM, 0)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    override fun onClick(v: View?) {
        if (isNavigationVisible) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
        isNavigationVisible = !isNavigationVisible
    }

    private fun hideSystemUI() {
        decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        pivImages.bottomMargin = resources.getDimensionPixelSize(R.dimen.padding_vertical)
    }

    private fun showSystemUI() {
        decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        pivImages.bottomMargin = getNavigationBarHeight() + resources.getDimensionPixelSize(R.dimen.padding_vertical)
    }

    private fun getNavigationBarHeight(): Int {
        val hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey()
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0 && !hasMenuKey) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    private var PageIndicatorView.bottomMargin: Int
        set(value) {
            val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = value
            pivImages.layoutParams = layoutParams
        }
        get() = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

    class RegistrationAdapter(private val images: List<String>, private val onClickListener: View.OnClickListener) : PagerAdapter() {
        override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageUrl = images[position]
            val view = LayoutInflater.from(container.context).inflate(R.layout.item_photo_pager, container, false)
            view.ivImage.image(imageUrl, crop = false)
            view.ivImage.setOnClickListener(onClickListener)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getCount(): Int = images.size
    }

    companion object {
        private const val EXTRA_IMAGES = "EXTRA_IMAGES"
        private const val EXTRA_SELECTED_ITEM = "EXTRA_SELECTED_ITEM"

        fun start(context: Context, images: List<String>, selectedItem: Int = 0) {
            context.startActivity(Intent(context, PhotoPagerActivity::class.java)
                    .putExtra(EXTRA_IMAGES, ArrayList(images))
                    .putExtra(EXTRA_SELECTED_ITEM, selectedItem))
        }
    }
}
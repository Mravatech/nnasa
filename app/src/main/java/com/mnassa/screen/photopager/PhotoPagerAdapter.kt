package com.mnassa.screen.photopager

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.mnassa.R
import com.mnassa.extensions.image
import kotlinx.android.synthetic.main.item_photo_pager.view.*

/**
 * Created by Peter on 9/11/2018.
 */
class PhotoPagerAdapter(private val images: List<String>, private val onClickListener: View.OnClickListener) : PagerAdapter() {
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val context = container.context

        val imageUrl = images[position]
        val view = LayoutInflater.from(context).inflate(R.layout.item_photo_pager, container, false)

        val imageView = (view.photoView) as ImageView
        imageView.image(imageUrl, crop = false)
        imageView.setOnClickListener(onClickListener)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int = images.size

}
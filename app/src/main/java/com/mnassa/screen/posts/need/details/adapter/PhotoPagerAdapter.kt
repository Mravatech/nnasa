package com.mnassa.screen.posts.need.details.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.mnassa.R
import com.mnassa.extensions.image
import kotlinx.android.synthetic.main.item_image.view.*

class PhotoPagerAdapter(private val images: List<String>, private val onClickListener: (String) -> Unit) : PagerAdapter() {
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageUrl = images[position]

        val view = LayoutInflater.from(container.context).inflate(R.layout.item_image, container, false)
        view.ivImage.image(imageUrl)
        view.setOnClickListener { onClickListener(imageUrl) }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) = container.removeView(view as View)
    override fun getCount(): Int = images.size
}
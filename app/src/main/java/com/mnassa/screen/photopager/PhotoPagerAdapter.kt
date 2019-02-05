package com.mnassa.screen.photopager

import android.net.Uri
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.GlideImageViewFactory
import com.mnassa.R
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

        val imageView = (view.bigImageView) as BigImageView
        imageView.setImageViewFactory(GlideImageViewFactory())
        imageView.showImage(Uri.parse(imageUrl))
        imageView.setOnClickListener(onClickListener)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        val imageView = (view.bigImageView) as BigImageView
        imageView.ssiv.recycle()
        container.removeView(view)
    }

    override fun getCount(): Int = images.size

}
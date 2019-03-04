package com.mnassa.screen.photopager

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.bundleOf
import com.mnassa.extensions.isInvisible
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.activity_photo_pager.*
import kotlinx.android.synthetic.main.activity_photo_pager.view.*
import org.kodein.di.generic.instance

/**
 * Created by Peter on 9/11/2018.
 */
class PhotoPagerController(args: Bundle) : MnassaControllerImpl<PhotoPagerViewModel>(args), View.OnClickListener {
    override val layoutId: Int = R.layout.activity_photo_pager
    override val viewModel: PhotoPagerViewModel by instance()
    private val images by lazy { args.getStringArrayList(EXTRA_IMAGES) }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        if (images.isEmpty()) {
            close()
            return
        }

        pager.adapter = PhotoPagerAdapter(images, this)
        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) = onPageChanged()
        })

        toolbar.inflateMenu(R.menu.image_preview)
        toolbar.menu.findItem(R.id.actionDownloadImage).title = fromDictionary(R.string.save_to_gallery)
        toolbar.setOnMenuItemClickListener {
            launchCoroutineUI {
                if (permissions.requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).isAllGranted) {
                    images.getOrNull(pager.currentItem)?.let { viewModel.loadImage(it) }
                }
            }
            true
        }

        args.getInt(EXTRA_SELECTED_ITEM, -1).takeIf { it >= 0 }?.let {
            pager.currentItem = it
            args.remove(EXTRA_SELECTED_ITEM)
        }

        onPageChanged()
    }

    private fun onPageChanged() {
        val view = view ?: return
        val pageNum = view.pager.currentItem
        view.toolbar.title = fromDictionary(R.string.gallery_image_num).format(pageNum + 1, images.size)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.actionDownloadImage -> {
                val image = images.getOrNull(pager.currentItem)
                viewModel.loadImage(image ?: return)
            }
            else -> toggleSystemUI()
        }
    }

    private fun toggleSystemUI() {
        val view = view ?: return
        view.toolbar.isInvisible = !view.toolbar.isInvisible
    }

    companion object {
        private const val EXTRA_IMAGES = "EXTRA_IMAGES"
        private const val EXTRA_SELECTED_ITEM = "EXTRA_SELECTED_ITEM"

        fun newInstance(images: List<String>, selectedItem: Int = 0): PhotoPagerController {
            return PhotoPagerController(
                    bundleOf {
                        putStringArrayList(EXTRA_IMAGES, images.toCollection(ArrayList()))
                        putInt(EXTRA_SELECTED_ITEM, selectedItem)
                    }
            )
        }
    }
}
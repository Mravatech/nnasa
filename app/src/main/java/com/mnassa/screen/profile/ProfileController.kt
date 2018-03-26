package com.mnassa.screen.profile

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.ImageView
import com.github.salomonbrys.kodein.instance
import com.google.firebase.storage.StorageReference
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.dialog.DialogHelper
import com.mnassa.extensions.avatarSquare
import com.mnassa.module.GlideApp
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_profile.view.*
import kotlinx.android.synthetic.main.profile_information.view.*
import kotlinx.android.synthetic.main.profile_toolbar.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class ProfileController : MnassaControllerImpl<ProfileViewModel>() {

    override val layoutId: Int = R.layout.controller_profile
    override val viewModel: ProfileViewModel by instance()

    private val dialog: DialogHelper by instance()
    var adapter = ProfileAdapter()
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            //            btnTakePhotoFromCamera.setOnClickListener {
//                dialog.showSelectImageSourceDialog(it.context) { imageSource ->
//                    activity?.let {
//                        val intent = CropActivity.start(imageSource, it)
//                        startActivityForResult(intent, REQUEST_CODE_CROP)
//                    }
//                }
//            }
            val list = mutableListOf<String>()//todo remove
            for (i in 0..20) {
                list.add("SIMPLE TEXT")
            }
            rvProfile.layoutManager = LinearLayoutManager(view.context)
            rvProfile.adapter = adapter
            adapter.set(list)

        }

        view.tvMoreInformation.setOnClickListener {
            view.profileInfo.visibility = if (view.profileInfo.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        onActivityResult.subscribe {
            when (it.requestCode) {
                REQUEST_CODE_CROP -> {
                    when (it.resultCode) {
                        Activity.RESULT_OK -> {
                            val uri: Uri? = it.data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                            uri?.let {
                                viewModel.uploadPhotoToStorage(it)
                            } ?: run {
                                Timber.i("uri is null")
                            }
                        }
                        CropActivity.GET_PHOTO_ERROR -> {
                            Timber.i("CropActivity.GET_PHOTO_ERROR")
                        }
                    }
                }
            }
        }
        launchCoroutineUI {
            viewModel.imageUploadedChannel.consumeEach {
                setImage(view.ivCropImage, it)
            }
        }
//        launchCoroutineUI {
//            viewModel.tagChannel.consumeEach {
//                for (tag in it){
//                    val chipView = ChipView(view.context, tag,0L, null)
//                    val params = FlowLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                    chipView.layoutParams = params
//                    view.flTags.addView(chipView)
//                }
//            }
//        }
        launchCoroutineUI {
            viewModel.profileChannel.consumeEach {
                view.ivCropImage.avatarSquare(it.avatar)
                view.toolbarProfile.title = "${it.personalInfo?.firstName} ${it.personalInfo?.lastName}"
                view.toolbarProfile.subtitle = "${it.personalInfo?.firstName} ${it.personalInfo?.lastName}"
            }
        }
        launchCoroutineUI {
            viewModel.allConnectionsCountChannel.consumeEach {
                val count = it.toString()
                val value = "$count ${fromDictionary(R.string.profile_connections)}"
                val span = SpannableString(value)
                span.setSpan(ForegroundColorSpan(Color.BLACK), 0, count.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(RelativeSizeSpan(1.5f), 0, count.length, 0)
                view.profileToolbar.tvProfileConnections.text = span
            }
        }

    }

    private fun setImage(imageView: ImageView, result: StorageReference?) {
        GlideApp.with(imageView).load(result).into(imageView)
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101

        fun newInstance() = ProfileController()
    }
}
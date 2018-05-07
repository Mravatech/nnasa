package com.mnassa.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.MediaController
import com.mnassa.R
import kotlinx.android.synthetic.main.activity_video_view.*


/**
 * Created by Peter on 4/30/2018.
 */
class VideoViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)

        videoView.setVideoURI(intent.getParcelableExtra(EXTRA_VIDEO_URI))

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoView.start()
    }

    companion object {
        private const val EXTRA_VIDEO_URI = "EXTRA_VIDEO_URI"
        private const val EXTRA_PREVIEW_URL = "EXTRA_PREVIEW_URL"

        fun start(context: Context, videoUri: Uri, previewUrl: String? = null) {
            context.startActivity(Intent(context, VideoViewActivity::class.java)
                    .putExtra(EXTRA_VIDEO_URI, videoUri)
                    .putExtra(EXTRA_PREVIEW_URL, previewUrl))
        }
    }
}
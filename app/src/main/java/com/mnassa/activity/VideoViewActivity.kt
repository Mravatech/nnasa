package com.mnassa.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.mnassa.R
import kotlinx.android.synthetic.main.activity_video_view.*


/**
 * Created by Peter on 4/30/2018.
 */
class VideoViewActivity : AppCompatActivity(), PlaybackPreparer {

    private var player: SimpleExoPlayer? = null
    private var mediaSource: MediaSource? = null

    private val dataSourceFactory: DataSource.Factory = DefaultHttpDataSourceFactory(PLAYER_USER_AGENT)

    private var startAutoPlay: Boolean = true
    private var startWindow: Int = 0
    private var startPosition: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)
    }

    public override fun onStart() {
        super.onStart()
        if (SDK_INT > Build.VERSION_CODES.M) {
            initializePlayer()
            if (videoView != null) {
                videoView.onResume()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (SDK_INT <= Build.VERSION_CODES.M || player == null) {
            initializePlayer()
            if (videoView != null) {
                videoView.onResume()
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        if (SDK_INT <= Build.VERSION_CODES.M) {
            if (videoView != null) {
                videoView.onPause()
            }
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (SDK_INT > Build.VERSION_CODES.M) {
            if (videoView != null) {
                videoView.onPause()
            }
            releasePlayer()
        }
    }

    override fun preparePlayback() {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (player == null) {
            val uris = intent.getParcelableExtra(EXTRA_VIDEO_URI) as Uri
            if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
                // The player will be reinitialized if the permission is granted.
                return
            }

            player = ExoPlayerFactory.newSimpleInstance(this)
                .apply {
                    playWhenReady = startAutoPlay
                }

            videoView.player = player
            videoView.setPlaybackPreparer(this)

            mediaSource = buildMediaSource(uris)
        }

        val haveStartPosition = startWindow != C.INDEX_UNSET
        if (haveStartPosition) {
            player?.seekTo(startWindow, startPosition)
        }

        player?.prepare(mediaSource, !haveStartPosition, false)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    private fun releasePlayer() {
        player?.apply {
            updateStartPosition()

            release()
            mediaSource = null
        }
        player = null
    }

    private fun Player.updateStartPosition() {
        startAutoPlay = playWhenReady
        startWindow = currentWindowIndex
        startPosition = Math.max(0, contentPosition)
    }

    companion object {
        private const val PLAYER_USER_AGENT = "exoplayer-mnassa"

        private const val EXTRA_VIDEO_URI = "EXTRA_VIDEO_URI"
        private const val EXTRA_PREVIEW_URL = "EXTRA_PREVIEW_URL"

        fun start(context: Context, videoUri: Uri, previewUrl: String? = null) {
            context.startActivity(
                Intent(context, VideoViewActivity::class.java)
                    .putExtra(EXTRA_VIDEO_URI, videoUri)
                    .putExtra(EXTRA_PREVIEW_URL, previewUrl)
            )
        }
    }
}
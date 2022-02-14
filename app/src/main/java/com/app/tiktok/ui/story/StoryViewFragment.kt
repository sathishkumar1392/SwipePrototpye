package com.app.tiktok.ui.story

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app.tiktok.R
import com.app.tiktok.app.MyApp
import com.app.tiktok.model.StoriesDataModel
import com.app.tiktok.ui.main.viewmodel.MainViewModel
import com.app.tiktok.utils.*
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.layout_story_view.*

class StoryViewFragment : Fragment(R.layout.fragment_story_view) {
    private var storyUrl: String? = null
    private var storiesDataModel: StoriesDataModel? = null

    private var simplePlayer: SimpleExoPlayer? = null
    private var cacheDataSourceFactory: CacheDataSourceFactory? = null
    private val simpleCache = MyApp.simpleCache
    private var toPlayVideoPosition: Int = -1

    companion object {
        fun newInstance(storiesDataModel: StoriesDataModel) = StoryViewFragment()
            .apply {
                arguments = Bundle().apply {
                    putParcelable(Constants.KEY_STORY_DATA, storiesDataModel)
                }
            }
    }

    private val viewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storiesDataModel = arguments?.getParcelable(Constants.KEY_STORY_DATA)
        setData()
        //setupPlayerView()
    }

    private fun setData() {
        text_view_account_handle.setTextOrHide(value = storiesDataModel?.userName)
        text_view_video_description.setTextOrHide(value = storiesDataModel?.storyDescription)
        text_view_music_title.setTextOrHide(value = storiesDataModel?.musicCoverTitle)

        image_view_option_comment_title?.text = storiesDataModel?.commentsCount?.formatNumberAsReadableFormat()
        image_view_option_like_title?.text = storiesDataModel?.likesCount?.formatNumberAsReadableFormat()

        image_view_profile_pic?.loadCenterCropImageFromUrl(storiesDataModel?.userProfilePicUrl)

        text_view_music_title.isSelected = true

        val simplePlayer = getPlayer()
        player_view_story.player = simplePlayer

        storyUrl = storiesDataModel?.storyUrl
        storyUrl?.let { prepareMedia(it) }

    }

    override fun onPause() {
        pauseVideo()
        super.onPause()
    }

    override fun onResume() {
        restartVideo()
        super.onResume()
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    private val playerCallback: Player.EventListener? = object: Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            logError("onPlayerStateChanged playbackState: $playbackState")
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            if (error != null) {
                super.onPlayerError(error)
            }
        }
    }

    private fun prepareVideoPlayer() {
        context?.let {
            simplePlayer = ExoPlayerFactory.newSimpleInstance(it)
            cacheDataSourceFactory = simpleCache?.let { it1 ->
                CacheDataSourceFactory(
                    it1,
                    DefaultHttpDataSourceFactory(
                        Util.getUserAgent(it,
                            "exo"))
                )
            }
        }
    }

    private fun getPlayer(): SimpleExoPlayer? {
        if (simplePlayer == null) {
            prepareVideoPlayer()
        }
        return simplePlayer
    }

    private fun prepareMedia(linkUrl: String) {
        logError("prepareMedia linkUrl: $linkUrl")
        if (linkUrl.endsWith(".m3u8")) {
               val uri1 = Uri.parse(linkUrl)
         val hls =  HlsMediaSource.Factory(cacheDataSourceFactory).createMediaSource(uri1)
            if (hls != null) {
                simplePlayer?.prepare(hls, true, true)
                simplePlayer?.repeatMode = Player.REPEAT_MODE_ONE
                //simplePlayer?.playWhenReady = true
                if (playerCallback != null) {
                    simplePlayer?.addListener(playerCallback)
                }
            }
        }else {
            val uri = Uri.parse(linkUrl)

            val mediaSource = cacheDataSourceFactory?.let { ProgressiveMediaSource.Factory(it).createMediaSource(uri) }
            if (mediaSource != null) {
                simplePlayer?.prepare(mediaSource, true, true)
                simplePlayer?.repeatMode = Player.REPEAT_MODE_ONE
                //simplePlayer?.playWhenReady = true
                if (playerCallback != null) {
                    simplePlayer?.addListener(playerCallback)
                }
            }
        }



        toPlayVideoPosition = -1
    }

    private fun setArtwork(drawable: Drawable, playerView: PlayerView) {
        playerView.useArtwork = true
        playerView.defaultArtwork = drawable
    }

    private fun playVideo() {
        simplePlayer?.playWhenReady = true
    }

    private fun setupPlayerView() {
        player_view_story.player = simplePlayer
    }

    private fun restartVideo() {
        if (simplePlayer == null) {
            storyUrl?.let { prepareMedia(it) }
        } else {
            simplePlayer?.seekToDefaultPosition()
            simplePlayer?.playWhenReady = true
        }
    }

    private fun pauseVideo() {
        simplePlayer?.playWhenReady = false
    }

    private fun releasePlayer() {
        simplePlayer?.stop(true)
        simplePlayer?.release()
    }
}
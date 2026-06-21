package com.example.musicplayer

import android.net.Uri
import android.view.Surface
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class VideoScreen(carContext: CarContext) : Screen(carContext) {

    private var exoPlayer: ExoPlayer? = null
    private var isPlaying = false
    private var activeSurface: Surface? = null

    // A sample public MP4 video url
    private val videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
            val surface = surfaceContainer.surface
            if (surface != null) {
                activeSurface = surface
                exoPlayer?.setVideoSurface(surface)
            }
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
            activeSurface = null
            exoPlayer?.setVideoSurface(null)
        }
    }

    init {
        val appManager = carContext.getCarService(AppManager::class.java)
        appManager.setSurfaceCallback(surfaceCallback)
        
        exoPlayer = ExoPlayer.Builder(carContext).build()
        exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                invalidate()
            }
        })

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()

        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
                exoPlayer?.release()
                exoPlayer = null
            }
        })
    }

    override fun onGetTemplate(): Template {
        val playPauseAction = Action.Builder()
            .setTitle(if (isPlaying) "Pause" else "Play")
            .setOnClickListener {
                if (isPlaying) {
                    exoPlayer?.pause()
                } else {
                    exoPlayer?.play()
                }
            }
            .build()

        return NavigationTemplate.Builder()
            .setActionStrip(
                androidx.car.app.model.ActionStrip.Builder()
                    .addAction(playPauseAction)
                    .build()
            )
            .build()
    }
}

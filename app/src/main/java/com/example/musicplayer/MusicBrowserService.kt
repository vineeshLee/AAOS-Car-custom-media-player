package com.example.musicplayer

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class MusicBrowserService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var exoPlayer: ExoPlayer

    private val songs = listOf(
        MediaItem.Builder()
            .setMediaId("song_1")
            .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
            .setMediaMetadata(com.google.android.exoplayer2.MediaMetadata.Builder().setTitle("Electronic Synth").build())
            .build(),
        MediaItem.Builder()
            .setMediaId("song_2")
            .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3")
            .setMediaMetadata(com.google.android.exoplayer2.MediaMetadata.Builder().setTitle("Upbeat Techno").build())
            .build(),
        MediaItem.Builder()
            .setMediaId("song_3")
            .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3")
            .setMediaMetadata(com.google.android.exoplayer2.MediaMetadata.Builder().setTitle("Chill Vibes").build())
            .build(),
        MediaItem.Builder()
            .setMediaId("song_4")
            .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3")
            .setMediaMetadata(com.google.android.exoplayer2.MediaMetadata.Builder().setTitle("Acoustic Melody").build())
            .build(),
        MediaItem.Builder()
            .setMediaId("song_5")
            .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3")
            .setMediaMetadata(com.google.android.exoplayer2.MediaMetadata.Builder().setTitle("Lo-Fi Beats").build())
            .build()
    )

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.setMediaItems(songs)
        exoPlayer.prepare()

        mediaSession = MediaSessionCompat(this, "MusicBrowserService")
        sessionToken = mediaSession.sessionToken
        mediaSession.isActive = true

        // Set initial queue for Now Playing screen
        val queue = songs.mapIndexed { index, song ->
            MediaSessionCompat.QueueItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(song.mediaId)
                    .setTitle(song.mediaMetadata.title)
                    .build(),
                index.toLong()
            )
        }
        mediaSession.setQueue(queue)

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                exoPlayer.play()
                updatePlaybackState()
            }

            override fun onPause() {
                exoPlayer.pause()
                updatePlaybackState()
            }

            override fun onSkipToNext() {
                exoPlayer.seekToNext()
                updatePlaybackState()
            }

            override fun onSkipToPrevious() {
                exoPlayer.seekToPrevious()
                updatePlaybackState()
            }

            override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
                val index = songs.indexOfFirst { it.mediaId == mediaId }
                if (index != -1) {
                    exoPlayer.seekTo(index, 0)
                    exoPlayer.play()
                    updatePlaybackState()
                }
            }
        })

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateMetadata()
            }
            
            override fun onPlayerError(error: com.google.android.exoplayer2.PlaybackException) {
                android.util.Log.e("MusicBrowserService", "ExoPlayer Error: \${error.message}", error)
            }
        })
        
        // Initialize metadata to first song so Now Playing isn't empty
        exoPlayer.seekTo(0, 0)
        updateMetadata()
        updatePlaybackState()
    }

    private fun updateMetadata() {
        val currentItem = exoPlayer.currentMediaItem
        if (currentItem != null) {
            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentItem.mediaMetadata.title.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, currentItem.mediaId)
                .build()
            mediaSession.setMetadata(metadata)
        }
    }

    private fun updatePlaybackState() {
        val state = if (exoPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, exoPlayer.currentPosition, 1.0f)
                .setActions(actions)
                .build()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == "root") {
            // AAOS Media Center requires tabs (browsable items) at the root level.
            val tabs = mutableListOf(
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId("all_songs")
                        .setTitle("All Songs")
                        .build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                )
            )
            result.sendResult(tabs)
        } else if (parentId == "all_songs") {
            val mediaItems = songs.map { song ->
                val description = MediaDescriptionCompat.Builder()
                    .setMediaId(song.mediaId)
                    .setTitle(song.mediaMetadata.title)
                    .build()
                MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
            }.toMutableList()
            result.sendResult(mediaItems)
        } else {
            result.sendResult(mutableListOf())
        }
    }
}

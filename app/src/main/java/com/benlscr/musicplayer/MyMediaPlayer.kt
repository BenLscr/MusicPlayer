package com.benlscr.musicplayer

import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object MyMediaPlayer {

    private var mediaPlayer = MediaPlayer()

    fun readOrPause(): Boolean {
        return if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            false
        } else {
            mediaPlayer.start()
            true
        }
    }

    fun startMusic(context: Context, id: Long) {
        val contentUri: Uri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        mediaPlayer = MediaPlayer.create(context, contentUri)
        mediaPlayer.start()
    }

    fun stopMusic() = if (mediaPlayer.isPlaying) { mediaPlayer.stop() } else {}

}
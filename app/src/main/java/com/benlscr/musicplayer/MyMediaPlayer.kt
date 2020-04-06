package com.benlscr.musicplayer

import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object MyMediaPlayer {

    private var mediaPlayer = MediaPlayer()

    fun isPlaying(): Boolean = mediaPlayer.isPlaying

    fun startNewMusic(context: Context, id: Long) {
        if (isPlaying()) {
            stop()
        }
        val contentUri: Uri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        mediaPlayer = MediaPlayer.create(context, contentUri)
        start()
    }

    fun start() = mediaPlayer.start()

    fun pause() = mediaPlayer.pause()

    fun stop() = mediaPlayer.stop()

    fun release() = mediaPlayer.release()

    fun currentPosition(): Int = mediaPlayer.currentPosition

}
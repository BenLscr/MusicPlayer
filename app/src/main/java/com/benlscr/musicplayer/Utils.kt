package com.benlscr.musicplayer

import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class Utils {

    companion object {

        private var mediaPlayer = MediaPlayer()

        fun readOrPause() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
            }
        }

        fun startMusic(context: Context, id: Long) {
            val contentUri: Uri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            mediaPlayer = MediaPlayer.create(context, contentUri)
            mediaPlayer.start()
        }

        fun stopMusic() = if (mediaPlayer.isPlaying) { mediaPlayer.stop() } else {}

    }

}
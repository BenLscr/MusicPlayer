package com.benlscr.musicplayer

import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.benlscr.musicplayer.expand.ExpandViewModel
import com.benlscr.musicplayer.shrunk.ShrunkViewModel

object MyMediaPlayer : MediaPlayer.OnCompletionListener {

    private var mediaPlayer = MediaPlayer()
    private var shrunkViewModel: ShrunkViewModel? = null
    private var expandViewModel: ExpandViewModel? = null

    fun isPlaying(): Boolean = mediaPlayer.isPlaying

    fun keepTheViewModel(viewModel: ViewModel) {
        when (viewModel) {
            is ShrunkViewModel -> {
                shrunkViewModel = viewModel
                expandViewModel = null
            }
            is ExpandViewModel -> {
                shrunkViewModel = null
                expandViewModel = viewModel
            }
        }
    }

    fun startNewMusic(context: Context, id: Long) {
        clearMediaPlayer()
        val contentUri: Uri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        //mediaPlayer = MediaPlayer.create(context, contentUri)
        mediaPlayer.apply {
            setDataSource(context, contentUri)
            setOnCompletionListener(this@MyMediaPlayer)
            prepare()
        }
        start()
    }

    private fun clearMediaPlayer() {
        stop()
        reset()
    }

    fun start() = mediaPlayer.start()

    fun pause() = mediaPlayer.pause()

    fun stop() = mediaPlayer.stop()

    fun reset() = mediaPlayer.reset()

    fun release() = mediaPlayer.release()

    fun currentPosition(): Int = mediaPlayer.currentPosition

    override fun onCompletion(p0: MediaPlayer?) {
        reset()
        when {
            shrunkViewModel != null -> shrunkViewModel?.whenMusicEndSkipToTheNext(true)
            expandViewModel != null -> expandViewModel?.whenMusicEndSkipToTheNext(true)
        }
    }

}
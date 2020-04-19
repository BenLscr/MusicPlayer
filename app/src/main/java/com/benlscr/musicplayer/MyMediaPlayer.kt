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

    fun prepare(context: Context, id: Long) {
        clearMediaPlayer()
        val contentUri: Uri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id
        )
        //mediaPlayer = MediaPlayer.create(context, contentUri)
        mediaPlayer.apply {
            setDataSource(context, contentUri)
            setOnCompletionListener(this@MyMediaPlayer)
            prepare()
        }
    }

    fun start() = mediaPlayer.start()

    fun pause() = mediaPlayer.pause()

    fun stop() = mediaPlayer.stop()

    fun reset() = mediaPlayer.reset()

    fun seekTo(milliseconds: Int) = mediaPlayer.seekTo(milliseconds)

    fun restart() = mediaPlayer.seekTo(0)

    fun release() = mediaPlayer.release()

    fun currentPosition(): Int = mediaPlayer.currentPosition

    fun duration(): Int = mediaPlayer.duration

    override fun onCompletion(p0: MediaPlayer?) {
        reset()
        when {
            shrunkViewModel != null -> shrunkViewModel?.whenMusicEndSkipToTheNext()
            expandViewModel != null -> expandViewModel?.whenMusicEndSkipToTheNext()
        }
    }

    private fun clearMediaPlayer() {
        stop()
        reset()
    }

}
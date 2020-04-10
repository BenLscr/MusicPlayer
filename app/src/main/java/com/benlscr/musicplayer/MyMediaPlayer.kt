package com.benlscr.musicplayer

import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.benlscr.musicplayer.shrunk.ShrunkViewModel

object MyMediaPlayer : MediaPlayer.OnCompletionListener {

    private var mediaPlayer = MediaPlayer()
    private var shrunkViewModel: ShrunkViewModel? = null
    //private var expandViewModel: ExpandViewModel? = null

    fun isPlaying(): Boolean = mediaPlayer.isPlaying

    fun keepTheViewModel(viewModel: ViewModel) {
        // When expandViewModel will be dev, set null to the other viewModel
        when (viewModel) {
            is ShrunkViewModel -> shrunkViewModel = viewModel
            //is ExpandViewModel -> expandViewModel = viewModel
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
        /**
         * L'idée est que le VM est donnée à chaque fois que resume est déclenché par les activity
         */
        reset()
        when {
            shrunkViewModel != null -> shrunkViewModel?.whenMusicEndSkipToTheNext(true)
            //expandViewModel != null -> expandViewModel?.whenMusicEndSkipToTheNext(true)
        }
    }

}
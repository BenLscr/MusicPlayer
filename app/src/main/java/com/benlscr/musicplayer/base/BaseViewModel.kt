package com.benlscr.musicplayer.base

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.benlscr.musicplayer.MyMediaPlayer

abstract class BaseViewModel : ViewModel() {

    protected lateinit var context: Context
    private val _albumImage = MutableLiveData<Uri>()
    val albumImage: LiveData<Uri> = _albumImage
    private var isThisTheEnd: Boolean = false

    abstract fun lookForMusics(contentResolver: ContentResolver)
    abstract fun skipBackward()
    abstract fun skipForward()

    fun keepContextFromActivity(context: Context) {
        this.context = context
    }

    open fun playOrPause() {
        if (MyMediaPlayer.isPlaying()) {
            MyMediaPlayer.pause()
        } else {
            MyMediaPlayer.start()
        }
    }

    fun giveViewModelToMediaPlayer() =
        MyMediaPlayer.keepTheViewModel(this)

    fun showAlbumArtInConsole(albumId: Long) {
        val uri = Uri.parse("content://media/external/audio/albumart")
        val albumArtUri = ContentUris.withAppendedId(uri, albumId)
        // albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        _albumImage.value = albumArtUri
    }

    protected fun needToPlayOrJustPrepare(id: Long) {
        if (MyMediaPlayer.isPlaying() || isThisTheEnd) {
            if (isThisTheEnd) {
                isThisTheEnd = false
            }
            playNewMusic(id)
        } else {
            MyMediaPlayer.prepare(context, id)
        }
    }

    private fun playNewMusic(id: Long) {
        MyMediaPlayer.prepare(context, id)
        MyMediaPlayer.start()
    }

    fun whenMusicEndSkipToTheNext() {
        isThisTheEnd = true
        skipForward()
    }

}
package com.benlscr.musicplayer.expand

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.benlscr.musicplayer.MyMediaPlayer
import com.benlscr.musicplayer.model.Music

class ExpandViewModel : ViewModel() {

    private lateinit var context: Context
    private val _musics = MutableLiveData<List<Music>>()
    private val _currentMusic = MutableLiveData<Music>()
    val currentMusic: LiveData<Music> = _currentMusic
    private var currentIndex: Int = -1
    private val _albumImage = MutableLiveData<Uri>()
    val albumImage: LiveData<Uri> = _albumImage

    fun keepContextFromActivity(context: Context) {
        this.context = context
    }

    fun giveViewModelToMediaPlayer() =
        MyMediaPlayer.keepTheViewModel(this)

    fun lookForMusics(contentResolver: ContentResolver) {
        val resolver: ContentResolver = contentResolver
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = resolver.query(uri, null, null, null, null)
        when {
            cursor == null -> {
                Log.e("SearchForMusic", "Query error")
            }
            !cursor.moveToFirst() -> {
                Log.e("SearchForMusic", "No media on the device")
            }
            else -> {
                val idColumn: Int = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                val titleColumn: Int = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val artistColumn: Int = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
                val albumColumn: Int = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM)
                val albumIdColumn: Int = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID)
                fillMusicList(cursor, idColumn, titleColumn, artistColumn, albumColumn, albumIdColumn)
            }
        }
        cursor?.close()
    }

    private fun fillMusicList(cursor: Cursor, idColumn: Int, titleColumn: Int, artistColumn: Int, albumColumn: Int, albumIdColumn: Int) {
        val list = ArrayList<Music>()
        do {
            val thisId = cursor.getLong(idColumn)
            var thisTitle = cursor.getString(titleColumn)
            val thisArtist = cursor.getString(artistColumn)
            val thisAlbum = cursor.getString(albumColumn)
            val thisAlbumId = cursor.getLong(albumIdColumn)
            if (thisTitle.length >= 25) {
                thisTitle = thisTitle.substring(0, 30) + "..."
            }
            list.add(Music(thisId, thisTitle, thisArtist, thisAlbum, thisAlbumId))
        } while (cursor.moveToNext())
        _musics.value = list
    }

    fun idFromShrunkAct(idFromShrunkAct: Long) {
        _musics.value?.let { _musics ->
            _musics.forEachIndexed { index, music ->
                if (music.id == idFromShrunkAct) {
                    currentIndex = index
                    music.onlyUiNeedUpdate = true
                    music.needToBePlayed = MyMediaPlayer.isPlaying()
                    music.isInMediaPlayer = true
                    _currentMusic.value = music
                }
            }
        }
    }

    fun updateMediaPlayer(id: Long, needToBePlayed: Boolean, isInMediaPlayer: Boolean, onlyUiNeedUpdate: Boolean) {
        if (!onlyUiNeedUpdate) {
            if (needToBePlayed && isInMediaPlayer) {
                MyMediaPlayer.start()
            } else if (needToBePlayed && !isInMediaPlayer) {
                MyMediaPlayer.startNewMusic(context, id)
            } else if (!needToBePlayed && isInMediaPlayer) {
                MyMediaPlayer.pause()
            }
        }
    }

    fun showAlbumArtInConsole(albumId: Long) {
        val uri = Uri.parse("content://media/external/audio/albumart")
        val albumArtUri = ContentUris.withAppendedId(uri, albumId)
        // albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        _albumImage.value = albumArtUri
    }

    fun playOrPause() {
        // For the moment doesn't handle the first show if no music is currently in MediaPlayer
        val music = _currentMusic.value
        music?.onlyUiNeedUpdate = false
        music?.isInMediaPlayer = true
        music?.needToBePlayed = !MyMediaPlayer.isPlaying()
        _currentMusic.value = music
    }

}
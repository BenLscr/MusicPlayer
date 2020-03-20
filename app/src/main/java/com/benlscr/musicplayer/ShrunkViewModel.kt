package com.benlscr.musicplayer

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlscr.musicplayer.model.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShrunkViewModel() : ViewModel() {

    private val _musics = mutableListOf<Music>()
    val musics = MutableLiveData<List<Music>>()
    private var currentId: Long? = null

    fun searchForMusic(contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.Default) {
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
                    updateMusicList(cursor, idColumn, titleColumn, artistColumn)
                    withContext(Dispatchers.Main) {
                        musics.value = _musics
                    }
                }
            }
            cursor?.close()
        }
    }

    private fun updateMusicList(cursor: Cursor, idColumn: Int, titleColumn: Int, artistColumn: Int) {
        do {
            val thisId = cursor.getLong(idColumn)
            var thisTitle = cursor.getString(titleColumn)
            val thisArtist = cursor.getString(artistColumn)
            if (thisTitle.length >= 25) {
                thisTitle = thisTitle.substring(0, 25) + "..."
            }
            _musics.add(Music(thisId, thisTitle, thisArtist))
        } while (cursor.moveToNext())
    }

    fun eventFromList(context: Context, id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            if (id == currentId) {
                Utils.readOrPause()
            } else {
                currentId = id
                Utils.stopMusic()
                Utils.startMusic(context, id)
            }
        }
    }

}
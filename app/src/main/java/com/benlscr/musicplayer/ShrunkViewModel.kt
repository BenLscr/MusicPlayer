package com.benlscr.musicplayer

import android.content.ContentResolver
import android.database.Cursor
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
                    val titleColumn: Int = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                    val artistColumn: Int = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
                    val albumColumn: Int = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM)
                    updateMusicList(cursor, titleColumn, artistColumn)
                    withContext(Dispatchers.Main) {
                        musics.value = _musics
                    }
                }
            }
            cursor?.close()
        }
    }

    private fun updateMusicList(cursor: Cursor, titleColumn: Int, artistColumn: Int) {
        do {
            var thisTitle = cursor.getString(titleColumn)
            val thisArtist = cursor.getString(artistColumn)
            if (thisTitle.length >= 25) {
                thisTitle = thisTitle.substring(0, 25) + "..."
            }
            _musics.add(Music(thisTitle, thisArtist))
        } while (cursor.moveToNext())
    }

    /*fun readMusics(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.sound_file_1)
            mediaPlayer.start()
        }
    }*/

}
package com.benlscr.musicplayer

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.benlscr.musicplayer.model.Music

class ShrunkViewModel : ViewModel() {

    private val _musics = MutableLiveData<List<Music>>()
    val musics: LiveData<List<Music>> = _musics
    private val _albumImage = MutableLiveData<Uri>()
    val albumImage: LiveData<Uri> = _albumImage
    private val _albumAndArtist = MutableLiveData<String>()
    val albumAndArtist : LiveData<String> = _albumAndArtist

    override fun onCleared() {
        super.onCleared()
        MyMediaPlayer.release()
    }

    fun searchForMusic(contentResolver: ContentResolver) {
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

    fun updateAlbumArtInTheConsole(contentResolver: ContentResolver, albumId: Long) {
        val uri = Uri.parse("content://media/external/audio/albumart")
        val albumArtUri = ContentUris.withAppendedId(uri, albumId)
        // albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        _albumImage.value = albumArtUri
    }

    fun updateAlbumAndArtistPlayedInTheConsole(album: String, artist: String) {
        _albumAndArtist.value = "$album \u2022 $artist"
    }

}
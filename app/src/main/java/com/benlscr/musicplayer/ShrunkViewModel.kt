package com.benlscr.musicplayer

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.benlscr.musicplayer.model.Music

class ShrunkViewModel : ViewModel() {

    private lateinit var context: Context
    private val _musics = MutableLiveData<List<Music>>()
    val musics: LiveData<List<Music>> = _musics
    private val _currentMusic = MutableLiveData<Music>()
    val currentMusic: LiveData<Music> = _currentMusic
    private var currentIndex: Int = -1
    private val _albumImage = MutableLiveData<Uri>()
    val albumImage: LiveData<Uri> = _albumImage
    private val _albumAndArtist = MutableLiveData<String>()
    val albumAndArtist : LiveData<String> = _albumAndArtist

    override fun onCleared() {
        super.onCleared()
        MyMediaPlayer.release()
    }

    fun keepContextFromActivity(context: Context) {
        this.context = context
    }

    fun giveViewModelToMediaPlayer() = MyMediaPlayer.keepTheViewModel(this)

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

    fun updateCurrentMusic(idSelected: Long) {
        _musics.value!!.forEachIndexed { index, music ->
            if (music.id == idSelected && idSelected != _currentMusic.value?.id) {
                // If it's this music in the list and it's not the same currently in the MediaPlayer
                // The first music played take this way
                currentIndex = index
                music.needToBePlayed = true
                _currentMusic.value = music
            } else if (music.id == idSelected && idSelected == _currentMusic.value?.id) {
                // If it's this music in the list and it's the current music used by the MediaPlayer
                currentIndex = index
                music.needToBePlayed = !MyMediaPlayer.isPlaying()
                music.isInMediaPlayer = true
                _currentMusic.value = music
            }
        }
    }

    fun updateMediaPlayer(id: Long, needToBePlayed: Boolean, isInMediaPlayer: Boolean) {
        if (needToBePlayed && isInMediaPlayer) {
            MyMediaPlayer.start()
        } else if (needToBePlayed && !isInMediaPlayer) {
            MyMediaPlayer.startNewMusic(context, id)
        } else if (!needToBePlayed && isInMediaPlayer) {
            MyMediaPlayer.pause()
        }
    }

    fun updateAlbumArtInTheConsole(albumId: Long) {
        val uri = Uri.parse("content://media/external/audio/albumart")
        val albumArtUri = ContentUris.withAppendedId(uri, albumId)
        // albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        _albumImage.value = albumArtUri
    }

    fun updateAlbumAndArtistPlayedInTheConsole(album: String, artist: String) {
        _albumAndArtist.value = "$album \u2022 $artist"
    }

    fun skipBackward() {
        _musics.value?.let { _musics ->
            if (_musics.isNotEmpty() && currentIndex != -1) {
                if (MyMediaPlayer.currentPosition() < 5000) {
                    if (currentIndex == 0) {
                        currentIndex = _musics.size -1
                    } else if (currentIndex > 0) {
                        currentIndex -= 1
                    }
                    _musics[currentIndex].needToBePlayed = true
                    _currentMusic.value = _musics[currentIndex]
                } else {
                    /**
                     * MyMediaPlayer.restart with seekTo
                     */
                    MyMediaPlayer.startNewMusic(context, _musics[currentIndex].id)
                }
            } else {
                /**
                 * Need a snackbar for error
                 * */
            }
        }
    }

    fun skipForward() {
        _musics.value?.let { _musics ->
            if (_musics.isNotEmpty() && currentIndex != -1) {
                if (currentIndex == _musics.size -1) {
                    currentIndex = 0
                } else {
                    currentIndex += 1
                }
                _musics[currentIndex].needToBePlayed = true
                _currentMusic.value = _musics[currentIndex]
            } else {
                /**
                 * Need a snackbar for error
                 * */
            }
        }
    }

    fun whenMusicEndSkipToTheNext(isFinished: Boolean) {
        if (isFinished) {
            skipForward()
        }
    }

}
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
import com.benlscr.musicplayer.expand.model.Music

class ExpandViewModel : ViewModel() {

    private lateinit var context: Context
    private var needFirstMusic: Boolean? = null
    private val _musics = MutableLiveData<List<Music>>()
    private val _currentMusic = MutableLiveData<Music>()
    val currentMusic: LiveData<Music> = _currentMusic
    private var currentIndex: Int = -1
    private val _albumImage = MutableLiveData<Uri>()
    val albumImage: LiveData<Uri> = _albumImage
    private val _curMusicDuration = MutableLiveData<Int>()
    val curMusicDuration: LiveData<Int> = _curMusicDuration

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
                val albumIdColumn: Int = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID)
                fillMusicList(cursor, idColumn, titleColumn, artistColumn, albumIdColumn)
            }
        }
        cursor?.close()
    }

    private fun fillMusicList(cursor: Cursor, idColumn: Int, titleColumn: Int, artistColumn: Int, albumIdColumn: Int) {
        val list = ArrayList<Music>()
        do {
            val thisId = cursor.getLong(idColumn)
            val thisTitle = cursor.getString(titleColumn)
            val thisArtist = cursor.getString(artistColumn)
            val thisAlbumId = cursor.getLong(albumIdColumn)
            list.add(Music(thisId, thisTitle, thisArtist, thisAlbumId))
        } while (cursor.moveToNext())
        _musics.value = list
    }

    fun idFromShrunkAct(idFromShrunkAct: Long) {
        needFirstMusic = false
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

    fun showFirstMusic() {
        _musics.value?.let { _musics ->
            needFirstMusic = true
            currentIndex = 0
            val music = _musics[0]
            music.onlyUiNeedUpdate = true
            music.isInMediaPlayer = false
            music.needToBePlayed = false
            _currentMusic.value =_musics[0]
        }
    }

    fun updateMediaPlayer(id: Long, needToBePlayed: Boolean, isInMediaPlayer: Boolean, onlyUiNeedUpdate: Boolean) {
        if (!onlyUiNeedUpdate) {
            if (needToBePlayed && isInMediaPlayer) {
                MyMediaPlayer.start()
            } else if (needToBePlayed && !isInMediaPlayer) {
                MyMediaPlayer.startNewMusic(context, id)
                updateDurationValue()
            } else if (!needToBePlayed && isInMediaPlayer) {
                MyMediaPlayer.pause()
            }
        }
    }

    private fun updateDurationValue() {
        _curMusicDuration.value = MyMediaPlayer.duration()
    }

    fun showAlbumArtInConsole(albumId: Long) {
        val uri = Uri.parse("content://media/external/audio/albumart")
        val albumArtUri = ContentUris.withAppendedId(uri, albumId)
        // albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        _albumImage.value = albumArtUri
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
                    _musics[currentIndex].onlyUiNeedUpdate = false
                    _currentMusic.value = _musics[currentIndex]
                } else {
                    /**
                     * MyMediaPlayer.restart with seekTo
                     */
                    MyMediaPlayer.startNewMusic(
                        context,
                        _musics[currentIndex].id
                    )
                }
            } else {
                /**
                 * Need a snackbar for error
                 * */
            }
        }
    }

    fun playOrPause() {
        needFirstMusic?.let { needFirstMusic ->
            if (needFirstMusic) {
                this.needFirstMusic = false
                val music = _currentMusic.value
                music?.onlyUiNeedUpdate = false
                music?.needToBePlayed = true
                music?.isInMediaPlayer = false
                _currentMusic.value = music
            } else {
                val music = _currentMusic.value
                music?.onlyUiNeedUpdate = false
                music?.isInMediaPlayer = true
                music?.needToBePlayed = !MyMediaPlayer.isPlaying()
                _currentMusic.value = music
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
                _musics[currentIndex].onlyUiNeedUpdate = false
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

    fun milliSecondsToTimer(milliseconds: Int): String {
        var finalTimerString = ""
        var secondsString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60))
        val minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000)
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }

}
package com.benlscr.musicplayer.expand

import android.content.ContentResolver
import android.database.Cursor
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.benlscr.musicplayer.MyMediaPlayer
import com.benlscr.musicplayer.R
import com.benlscr.musicplayer.base.BaseViewModel
import com.benlscr.musicplayer.expand.model.Music

class ExpandViewModel : BaseViewModel() {

    private val _musics = MutableLiveData<List<Music>>()
    private val _currentMusic = MutableLiveData<Music>()
    val currentMusic: LiveData<Music> = _currentMusic
    private var currentIndex: Int = -1
    private val _curMusicDuration = MutableLiveData<Int>()
    val curMusicDuration: LiveData<Int> = _curMusicDuration
    private val _buttonPlayOrPause = MutableLiveData<Int>()
    val buttonPlayOrPause: LiveData<Int> = _buttonPlayOrPause

    override fun lookForMusics(contentResolver: ContentResolver) {
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
                val idColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                val titleColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val artistColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
                val albumIdColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID)
                fillMusicList(cursor, idColumn, titleColumn, artistColumn, albumIdColumn)
            }
        }
        cursor?.close()
    }

    private fun fillMusicList(
        cursor: Cursor,
        idColumn: Int,
        titleColumn: Int,
        artistColumn: Int,
        albumIdColumn: Int
    ) {
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
        _musics.value?.let { _musics ->
            _musics.forEachIndexed { index, music ->
                if (music.id == idFromShrunkAct) {
                    currentIndex = index
                    _currentMusic.value = music
                }
            }
            showPlayOrPauseButton()
        }
    }

    fun prepareFirstMusic() {
        _musics.value?.let { _musics ->
            currentIndex = 0
            MyMediaPlayer.prepare(context, _musics[0].id)
            _currentMusic.value = _musics[0]
            showPlayOrPauseButton()
        }
    }

    fun updateDurationValue() {
        _curMusicDuration.value = MyMediaPlayer.duration()
    }

    override fun skipBackward() {
        _musics.value?.let { _musics ->
            if (_musics.isNotEmpty() && currentIndex != -1) {
                if (MyMediaPlayer.currentPosition() < 5000
                    &&
                    MyMediaPlayer.isPlaying()
                    ||
                    MyMediaPlayer.currentPosition() == 0
                ) {
                    if (currentIndex == 0) {
                        currentIndex = _musics.size - 1
                    } else if (currentIndex > 0) {
                        currentIndex -= 1
                    }
                    _currentMusic.value = _musics[currentIndex]
                    needToPlayOrJustPrepare(_currentMusic.value!!.id)
                    showPlayOrPauseButton()
                } else {
                    MyMediaPlayer.restart()
                }
            } else {
                /**
                 * Need a snackbar for error
                 * */
            }
        }
    }

    override fun playOrPause() {
        super.playOrPause()
        showPlayOrPauseButton()
    }

    override fun skipForward() {
        _musics.value?.let { _musics ->
            if (_musics.isNotEmpty() && currentIndex != -1) {
                if (currentIndex == _musics.size - 1) {
                    currentIndex = 0
                } else {
                    currentIndex += 1
                }
                needToPlayOrJustPrepare(_musics[currentIndex].id)
                _currentMusic.value = _musics[currentIndex]
                showPlayOrPauseButton()
            } else {
                /**
                 * Need a snackbar for error
                 * */
            }
        }
    }

    private fun showPlayOrPauseButton() {
        if (MyMediaPlayer.isPlaying()) {
            _buttonPlayOrPause.value = R.drawable.console_pause_expand
        } else {
            _buttonPlayOrPause.value = R.drawable.console_play_expand
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

    fun eventOnSeekBarFromUser(progress: Int) {
        MyMediaPlayer.seekTo(progress)
    }

}
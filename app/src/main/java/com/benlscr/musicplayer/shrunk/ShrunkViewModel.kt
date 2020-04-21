package com.benlscr.musicplayer.shrunk

import android.content.ContentResolver
import android.database.Cursor
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.benlscr.musicplayer.MyMediaPlayer
import com.benlscr.musicplayer.R
import com.benlscr.musicplayer.base.BaseViewModel
import com.benlscr.musicplayer.shrunk.model.Music
import com.benlscr.musicplayer.shrunk.model.MusicItemList

class ShrunkViewModel : BaseViewModel() {

    private val _musics = MutableLiveData<List<Music>>()
    val musics: LiveData<List<Music>> = _musics
    private val _currentMusic = MutableLiveData<Music>()
    val currentMusic: LiveData<Music> = _currentMusic
    private var currentIndex: Int = -1
    private val _albumAndArtist = MutableLiveData<String>()
    val albumAndArtist: LiveData<String> = _albumAndArtist
    private val _musicsItemList = MutableLiveData<List<MusicItemList>>()
    val musicsItemList: LiveData<List<MusicItemList>> = _musicsItemList

    override fun onCleared() {
        super.onCleared()
        MyMediaPlayer.release()
    }

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
                val albumColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM)
                val albumIdColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID)
                fillMusicList(
                    cursor,
                    idColumn,
                    titleColumn,
                    artistColumn,
                    albumColumn,
                    albumIdColumn
                )
            }
        }
        cursor?.close()
    }

    private fun fillMusicList(
        cursor: Cursor,
        idColumn: Int,
        titleColumn: Int,
        artistColumn: Int,
        albumColumn: Int,
        albumIdColumn: Int
    ) {
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

    fun prepareMusicsItemList() {
        val list = ArrayList<MusicItemList>()
        _musics.value?.let { _musics ->
            for (music in _musics) {
                val musicItemList = MusicItemList(
                    id = music.id,
                    title = music.title,
                    artist = music.artist,
                    button = R.drawable.button_play_shrink,
                    background = null
                )
                list.add(musicItemList)
            }
        }
        _musicsItemList.value = list
    }

    fun eventFromListFragment(idSelected: Long) {
        if (_currentMusic.value?.id != idSelected) {
            MyMediaPlayer.prepare(context, idSelected)
        }
        playOrPause()
        updateMusicListForFragment(idSelected)
    }

    private fun updateMusicListForFragment(id: Long) {
        val list = ArrayList<MusicItemList>()
        _musics.value?.let { _musics ->
            _musics.forEachIndexed { index, music ->
                val musicItemList: MusicItemList
                if (music.id == id) {
                    currentIndex = index
                    _currentMusic.value = music
                    musicItemList = MusicItemList(
                        id = music.id,
                        title = music.title,
                        artist = music.artist,
                        button = if (MyMediaPlayer.isPlaying()) {
                            R.drawable.button_pause_shrink
                        } else {
                            R.drawable.button_play_shrink
                        },
                        background = R.drawable.background_current_music
                    )
                } else {
                    musicItemList = MusicItemList(
                        id = music.id,
                        title = music.title,
                        artist = music.artist,
                        button = R.drawable.button_play_shrink,
                        background = null
                    )
                }
                list.add(musicItemList)
            }
        }
        _musicsItemList.value = list
    }

    fun showAlbumAndArtistPlayedInConsole(album: String, artist: String) {
        _albumAndArtist.value = "$album \u2022 $artist"
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
                    updateMusicListForFragment(_currentMusic.value!!.id)
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

    override fun skipForward() {
        _musics.value?.let { _musics ->
            if (_musics.isNotEmpty() && currentIndex != -1) {
                if (currentIndex == _musics.size - 1) {
                    currentIndex = 0
                } else {
                    currentIndex += 1
                }
                _currentMusic.value = _musics[currentIndex]
                updateMusicListForFragment(_currentMusic.value!!.id)
                needToPlayOrJustPrepare(_currentMusic.value!!.id)
            } else {
                /**
                 * Need a snackbar for error
                 * */
            }
        }
    }

    fun idFromExpandActivity(idFromExpandAct: Long) {
        if (idFromExpandAct == _currentMusic.value?.id) {
            val music = _currentMusic.value
            _currentMusic.value = music
            updateMusicListForFragment(_currentMusic.value!!.id)
        } else {
            _musics.value?.let { _musics ->
                _musics.forEachIndexed { index, music ->
                    if (music.id == idFromExpandAct) {
                        currentIndex = index
                        _currentMusic.value = music
                        updateMusicListForFragment(music.id)
                    }
                }
            }
        }
    }

}
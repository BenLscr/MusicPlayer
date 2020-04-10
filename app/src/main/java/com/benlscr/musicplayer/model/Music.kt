package com.benlscr.musicplayer.model

class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    var needToBePlayed: Boolean = false,
    var isInMediaPlayer: Boolean = false,
    var onlyUiNeedUpdate: Boolean = false
)
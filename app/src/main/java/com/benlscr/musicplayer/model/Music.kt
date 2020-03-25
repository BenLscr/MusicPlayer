package com.benlscr.musicplayer.model

class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    var isCurrentlyPlayed: Boolean = false
)
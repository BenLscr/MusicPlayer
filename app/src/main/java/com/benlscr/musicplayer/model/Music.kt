package com.benlscr.musicplayer.model

class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    var isCurrentlyPlayed: Boolean = false
)
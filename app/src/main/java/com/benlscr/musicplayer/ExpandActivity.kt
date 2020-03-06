package com.benlscr.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class ExpandActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expand)
    }

    fun closeExpandActivity(view: View) {
        onBackPressed()
        //finish()
    }
}

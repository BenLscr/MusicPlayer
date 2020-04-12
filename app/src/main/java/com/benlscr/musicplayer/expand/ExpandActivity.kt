package com.benlscr.musicplayer.expand

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.benlscr.musicplayer.ID_FOR_EXPAND_ACT
import com.benlscr.musicplayer.ID_FOR_SHRUNK_ACT
import com.benlscr.musicplayer.R
import com.benlscr.musicplayer.databinding.ActivityExpandBinding
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_expand.*

class ExpandActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpandBinding
    private val expandViewModel: ExpandViewModel
            by lazy { ViewModelProvider(this).get(ExpandViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpandBinding.inflate(layoutInflater)
        setContentView(binding.root)

        giveContextToViewModel()
        lookForMusics()
        setObservers()
        retrievesIntent()
    }

    override fun onResume() {
        super.onResume()
        expandViewModel.giveViewModelToMediaPlayer()
    }

    private fun giveContextToViewModel() =
        expandViewModel.keepContextFromActivity(applicationContext)

    private fun lookForMusics() =
        expandViewModel.lookForMusics(contentResolver)

    private fun setObservers() {
        expandViewModel.currentMusic.observe(
            this,
            Observer { music ->
                expandViewModel.updateMediaPlayer(
                    music.id,
                    music.needToBePlayed,
                    music.isInMediaPlayer,
                    music.onlyUiNeedUpdate
                )
                expandViewModel.showAlbumArtInConsole(music.albumId)
                updateExpandUi(music.title, music.artist, music.needToBePlayed)
            }
        )
        expandViewModel.albumImage.observe(
            this,
            Observer { showAlbumArt(it) }
        )
    }

    private fun updateExpandUi(title: String, artist: String, needToBePlayed: Boolean) {
        binding.titleExpand.text = title
        binding.artistExpand.text = artist
        if (needToBePlayed) {
            binding.consolePlayExpand.setImageResource(R.drawable.console_pause_expand)
        } else {
            binding.consolePlayExpand.setImageResource(R.drawable.console_play_expand)
        }
    }

    private fun showAlbumArt(albumArt: Uri) =
        Glide.with(applicationContext)
            .asBitmap()
            .load(albumArt)
            .error(R.drawable.album_image)
            .into(binding.albumImageExpand)

    private fun retrievesIntent() {
        if (intent.hasExtra(ID_FOR_EXPAND_ACT)) {
            val idFromShrunkAct = intent.getLongExtra(ID_FOR_EXPAND_ACT, -1L)
            if (idFromShrunkAct != -1L) {
                expandViewModel.idFromShrunkAct(idFromShrunkAct)
            }
        } else {
            //showFirstMusic
        }
    }

    /* ---------- LISTENERS FROM LAYOUT ---------- */

    fun shrunkButton(view: View) {
        expandViewModel.currentMusic.value?.let { music ->
            if (music.isInMediaPlayer) {
                val intent = Intent()
                intent.putExtra(ID_FOR_SHRUNK_ACT, music.id)
                setResult(Activity.RESULT_CANCELED, intent)
            }
        }
        finish()
    }

    fun skipBackwardExpandButton(view: View) {

    }

    fun playPauseExpandButton(view: View) {
        expandViewModel.playOrPause()
    }

    fun skipForwardExpandButton(view: View) {

    }

}

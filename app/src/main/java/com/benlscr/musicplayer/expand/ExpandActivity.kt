package com.benlscr.musicplayer.expand

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.benlscr.musicplayer.ID_FOR_EXPAND_ACT
import com.benlscr.musicplayer.ID_FOR_SHRUNK_ACT
import com.benlscr.musicplayer.MyMediaPlayer
import com.benlscr.musicplayer.R
import com.benlscr.musicplayer.databinding.ActivityExpandBinding
import com.bumptech.glide.Glide

class ExpandActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpandBinding
    private val expandViewModel: ExpandViewModel
            by lazy { ViewModelProvider(this).get(ExpandViewModel::class.java) }
    private val seekHandler = Handler()

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
        expandViewModel.curMusicDuration.observe(
            this,
            Observer { duration ->
                setMaxSeekBar(duration)
                updateSeekBar()
            }
        )
        expandViewModel.albumImage.observe(
            this,
            Observer { showAlbumArt(it) }
        )
    }

    private fun setMaxSeekBar(duration: Int) {
        binding.seekBar.max = duration
        binding.endTime.text = expandViewModel.milliSecondsToTimer(duration)
    }

    private fun updateSeekBar() {
        binding.currentTime.text = expandViewModel.milliSecondsToTimer(MyMediaPlayer.currentPosition())
        binding.seekBar.progress = MyMediaPlayer.currentPosition()
        seekHandler.postDelayed(runnable, 100)
    }

    private val runnable = Runnable { updateSeekBar() }

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
            expandViewModel.showFirstMusic()
        }
    }

    /* ---------- LISTENERS FROM LAYOUT ---------- */

    fun shrunkButton(view: View) {
        expandViewModel.currentMusic.value?.let { music ->
            if (music.isInMediaPlayer || music.needToBePlayed) {
                val intent = Intent()
                intent.putExtra(ID_FOR_SHRUNK_ACT, music.id)
                setResult(Activity.RESULT_CANCELED, intent)
            }
        }
        finish()
    }

    fun skipBackwardExpandButton(view: View) {
        expandViewModel.skipBackward()
    }

    fun playPauseExpandButton(view: View) {
        expandViewModel.playOrPause()
    }

    fun skipForwardExpandButton(view: View) {
        expandViewModel.skipForward()
    }

}

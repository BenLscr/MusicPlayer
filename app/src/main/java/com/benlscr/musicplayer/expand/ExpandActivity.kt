package com.benlscr.musicplayer.expand

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
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
        manageSeekBarListener()
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
                expandViewModel.updateDurationValue()
                expandViewModel.showAlbumArtInConsole(music.albumId)
                updateTitleAndArtist(music.title, music.artist)
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
        expandViewModel.buttonPlayOrPause.observe(
            this,
            Observer { drawable ->
                updatePlayOrPauseDrawableButton(drawable)
            }
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

    private fun updateTitleAndArtist(title: String, artist: String) {
        binding.titleExpand.text = title
        binding.artistExpand.text = artist
    }

    private fun showAlbumArt(albumArt: Uri) =
        Glide.with(applicationContext)
            .asBitmap()
            .load(albumArt)
            .error(R.drawable.album_image)
            .into(binding.albumImageExpand)

    private fun updatePlayOrPauseDrawableButton(drawable: Int) =
        binding.consolePlayExpand.setImageResource(drawable)

    private fun retrievesIntent() {
        if (intent.hasExtra(ID_FOR_EXPAND_ACT)) {
            val idFromShrunkAct = intent.getLongExtra(ID_FOR_EXPAND_ACT, -1L)
            if (idFromShrunkAct != -1L) {
                expandViewModel.idFromShrunkAct(idFromShrunkAct)
            }
        } else {
            expandViewModel.prepareFirstMusic()
        }
    }

    private fun manageSeekBarListener() {
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    expandViewModel.eventOnSeekBarFromUser(progress)
                }
                /*fromUser.let {
                    expandViewModel.eventOnSeekBarFromUser(progress)
                }*/
            }
        })
    }

    /* ---------- LISTENERS FROM LAYOUT ---------- */

    fun shrunkButton(view: View) {
        expandViewModel.currentMusic.value?.let { music ->
            if (MyMediaPlayer.currentPosition() != 0) {
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

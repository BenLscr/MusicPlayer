package com.benlscr.musicplayer

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.benlscr.musicplayer.databinding.ActivityShrunkBinding
import com.benlscr.musicplayer.model.Music

import com.bumptech.glide.Glide
import com.sembozdemir.permissionskt.askPermissions
import kotlinx.android.synthetic.main.activity_shrunk.view.*

class ShrunkActivity : AppCompatActivity(), MusicsFragment.OnListFragmentInteractionListener {

    private lateinit var binding: ActivityShrunkBinding
    private val shrunkViewModel : ShrunkViewModel by lazy { ViewModelProvider(this).get(ShrunkViewModel::class.java) }
    private val musicsFragment = MusicsFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShrunkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addMusicsFragment()
        askPermissions()
        setObserver()
        openExpandActivity()
    }

    private fun addMusicsFragment() {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.musics_container, musicsFragment)
        fragmentTransaction.commit()
    }

    override fun onListFragmentInteraction(albumId: Long,  album: String, artist: String) {
        shrunkViewModel.updateAlbumArtInTheConsole(contentResolver, albumId)
        shrunkViewModel.updateAlbumAndArtistPlayedInTheConsole(album, artist)
    }

    private fun askPermissions() {
        askPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
            onGranted {
                searchForMusic()
            }
        }
    }

    private fun searchForMusic() {
        shrunkViewModel.searchForMusic(contentResolver)
    }

    private fun setObserver() {
        shrunkViewModel.musics.observe(
            this,
            Observer { updateMusicSFragment(it) }
        )
        shrunkViewModel.albumImage.observe(
            this,
            Observer { updateAlbumArt(it) }
        )
        shrunkViewModel.albumAndArtist.observe(
            this,
            Observer { updateAlbumAndArtist(it) }
        )
    }

    private fun updateMusicSFragment(musics: List<Music>) = musicsFragment.updateMusicsFragment(musics)

    private fun updateAlbumArt(albumArt: Uri) =
        Glide.with(applicationContext)
            .asBitmap()
            .load(albumArt)
            .error(R.drawable.album_image)
            .into(binding.albumImageShrink)

    private fun updateAlbumAndArtist(albumAndArtist: String) {
        binding.albumAndArtist.text = albumAndArtist
    }

    private fun openExpandActivity() {
        binding.expand.setOnClickListener {
            val intent = Intent(this, ExpandActivity::class.java)
            startActivity(intent)
        }
    }

}

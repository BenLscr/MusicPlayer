package com.benlscr.musicplayer.shrunk

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.benlscr.musicplayer.ID_FOR_EXPAND_ACT
import com.benlscr.musicplayer.ID_FOR_SHRUNK_ACT
import com.benlscr.musicplayer.R
import com.benlscr.musicplayer.REQUEST_CODE_SHRUNK_EXPAND
import com.benlscr.musicplayer.databinding.ActivityShrunkBinding
import com.benlscr.musicplayer.expand.ExpandActivity
import com.benlscr.musicplayer.shrunk.model.MusicItemList
import com.bumptech.glide.Glide
import com.sembozdemir.permissionskt.askPermissions

class ShrunkActivity : AppCompatActivity(),
    MusicsFragment.OnListFragmentInteractionListener {

    private lateinit var binding: ActivityShrunkBinding
    private val shrunkViewModel: ShrunkViewModel
            by lazy { ViewModelProvider(this).get(ShrunkViewModel::class.java) }
    private val musicsFragment =
        MusicsFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShrunkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addMusicsFragment()
        giveContextToViewModel()
        askPermissions()
        setObservers()
    }

    override fun onResume() {
        super.onResume()
        shrunkViewModel.giveViewModelToMediaPlayer()
    }

    private fun addMusicsFragment() {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.musics_container, musicsFragment)
        fragmentTransaction.commit()
    }

    override fun onListFragmentInteraction(idSelected: Long) {
        shrunkViewModel.eventFromListFragment(idSelected)
    }

    private fun giveContextToViewModel() =
        shrunkViewModel.keepContextFromActivity(applicationContext)

    private fun askPermissions() =
        askPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
            onGranted {
                lookForMusics()
            }
        }

    private fun lookForMusics() =
        shrunkViewModel.lookForMusics(contentResolver)


    private fun setObservers() {
        shrunkViewModel.musics.observe(
            this,
            Observer {
                shrunkViewModel.prepareMusicsItemList()
            }
        )
        shrunkViewModel.musicsItemList.observe(
            this,
            Observer { musics ->
                fillMusicsFragment(musics)
            }
        )
        shrunkViewModel.currentMusic.observe(
            this,
            Observer { music ->
                shrunkViewModel.showAlbumArtInConsole(music.albumId)
                shrunkViewModel.showAlbumAndArtistPlayedInConsole(music.album, music.artist)
            }
        )
        shrunkViewModel.albumImage.observe(
            this,
            Observer { uri ->
                showAlbumArt(uri)
            }
        )
        shrunkViewModel.albumAndArtist.observe(
            this,
            Observer { string ->
                showAlbumAndArtist(string)
            }
        )
    }

    private fun fillMusicsFragment(musics: List<MusicItemList>) =
        musicsFragment.updateMusicsFragment(musics)

    private fun showAlbumArt(albumArt: Uri) =
        Glide.with(applicationContext)
            .asBitmap()
            .load(albumArt)
            .error(R.drawable.album_image)
            .into(binding.albumImageShrink)

    private fun showAlbumAndArtist(albumAndArtist: String) {
        binding.albumAndArtist.text = albumAndArtist
    }

    /* ---------- LISTENERS FROM LAYOUT ---------- */

    fun expandButton(view: View) {
        val intent = Intent(this, ExpandActivity::class.java)
        val idForExpandAct: Long? = shrunkViewModel.currentMusic.value?.id
        idForExpandAct?.let { intent.putExtra(ID_FOR_EXPAND_ACT, it) }
        startActivityForResult(intent, REQUEST_CODE_SHRUNK_EXPAND)
    }

    fun skipBackwardShrunkButton(view: View) {
        shrunkViewModel.skipBackward()
    }

    fun skipForwardShrunkButton(view: View) {
        shrunkViewModel.skipForward()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SHRUNK_EXPAND) {
            if (resultCode == Activity.RESULT_CANCELED) {
                data?.let {
                    val idFromExpandAct = it.getLongExtra(ID_FOR_SHRUNK_ACT, -1L)
                    if (idFromExpandAct != -1L) {
                        shrunkViewModel.idFromExpandActivity(idFromExpandAct)
                    }
                }
            }
        }
    }

}

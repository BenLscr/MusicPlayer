package com.benlscr.musicplayer

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView


import com.benlscr.musicplayer.MusicsFragment.OnListFragmentInteractionListener
import com.benlscr.musicplayer.dummy.DummyContent.DummyItem
import com.benlscr.musicplayer.model.Music

import kotlinx.android.synthetic.main.fragment_music.view.*

class MusicRecyclerViewAdapter(
) : RecyclerView.Adapter<MusicRecyclerViewAdapter.ViewHolder>() {

    private val musics = mutableListOf<Music>()
    private var mListener: OnListFragmentInteractionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_music, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musics[position]

        with(holder) {
            title.text = music.title
            artist.text = music.artist
            playPauseShrink.setOnClickListener {
                mListener?.onListFragmentInteraction(music.id)
            }
            //mView.setOnClickListener { mListener?.onListFragmentInteraction(position) }
        }
    }

    override fun getItemCount(): Int = musics.size

    fun updateMusicsFragment(musics: List<Music>, listener: OnListFragmentInteractionListener?) {
        mListener = listener
        this.musics.clear()
        this.musics.addAll(musics)
        notifyDataSetChanged()
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val title: TextView = mView.title_list
        val artist: TextView = mView.artist_list
        val playPauseShrink: ImageButton = mView.play_pause_shrink
    }
}

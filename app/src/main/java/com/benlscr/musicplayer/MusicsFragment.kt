package com.benlscr.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benlscr.musicplayer.model.Music

class MusicsFragment : Fragment() {

    private val musicRecyclerViewAdapter = MusicRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music_list, container, false)

        (view as RecyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = musicRecyclerViewAdapter
        }
        return view
    }

    fun updateMusicsFragment(musics: List<Music>) = musicRecyclerViewAdapter.updateMusicsFragment(musics, context)

    companion object {
        @JvmStatic
        fun newInstance() =
            MusicsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

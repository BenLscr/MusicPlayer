package com.benlscr.musicplayer.shrunk

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benlscr.musicplayer.R
import com.benlscr.musicplayer.model.Music

class MusicsFragment : Fragment() {

    private val musicRecyclerViewAdapter =
        MusicRecyclerViewAdapter()
    private var listener: OnListFragmentInteractionListener? = null

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(idSelected: Long)
    }

    fun fillMusicsFragment(musics: List<Music>)
            = musicRecyclerViewAdapter.fillMusicsFragment(musics, listener, context)

    fun updateMusicsFragment(id: Long, needToBePlayed: Boolean, isInMediaPlayer: Boolean)
            = musicRecyclerViewAdapter.updateMusicsFragment(id, needToBePlayed, isInMediaPlayer)

    companion object {
        @JvmStatic
        fun newInstance() =
            MusicsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

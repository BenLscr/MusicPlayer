package com.benlscr.musicplayer.shrunk


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.benlscr.musicplayer.R
import com.benlscr.musicplayer.shrunk.MusicsFragment.OnListFragmentInteractionListener
import com.benlscr.musicplayer.shrunk.model.MusicItemList
import kotlinx.android.synthetic.main.fragment_music.view.*

class MusicRecyclerViewAdapter : RecyclerView.Adapter<MusicRecyclerViewAdapter.ViewHolder>() {

    private var mListener: OnListFragmentInteractionListener? = null
    private var mContext: Context? = null
    private val musics = mutableListOf<MusicItemList>()

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
            playPauseShrink.background = mContext?.getDrawable(music.button)
            musicBackground.background = music.background?.let { mContext?.getDrawable(it) }
            playPauseShrink.setOnClickListener {
                mListener?.onListFragmentInteraction(music.id)
            }
        }
    }

    override fun getItemCount(): Int = musics.size

    fun updateMusicsFragment(
        musics: List<MusicItemList>,
        listener: OnListFragmentInteractionListener?,
        context: Context?
    ) {
        mListener = listener
        mContext = context
        this.musics.clear()
        this.musics.addAll(musics)
        notifyDataSetChanged()
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val title: TextView = mView.title_list
        val artist: TextView = mView.artist_list
        val playPauseShrink: ImageButton = mView.play_pause_shrink
        val musicBackground: FrameLayout = mView.music_in_list_layout
    }
}

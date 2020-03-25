package com.benlscr.musicplayer


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.benlscr.musicplayer.model.Music
import kotlinx.android.synthetic.main.fragment_music.view.*

class MusicRecyclerViewAdapter(
) : RecyclerView.Adapter<MusicRecyclerViewAdapter.ViewHolder>() {

    private val musics = mutableListOf<Music>()
    private var mContext: Context? = null
    private var currentId: Long? = null

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
                mContext?.let { context -> eventFromList(context, position, music.id) }
            }
            playPauseShrink.background = mContext?.let {
                if (currentId == music.id && music.isCurrentlyPlayed) {
                    ContextCompat.getDrawable(it, R.drawable.button_pause_shrink)
                } else {
                    ContextCompat.getDrawable(it, R.drawable.button_play_shrink)
                }
            }
            musicBackground.background = mContext?.let {
                if (currentId == music.id) {
                    ContextCompat.getDrawable(it, R.drawable.background_current_music)
                } else {
                    null
                }
            }
        }
    }

    override fun getItemCount(): Int = musics.size

    fun updateMusicsFragment(musics: List<Music>, context: Context?) {
        mContext = context
        this.musics.clear()
        this.musics.addAll(musics)
        notifyDataSetChanged()
    }

    private fun eventFromList(context: Context, position: Int, id: Long) {
        val isPlaying: Boolean
        if (id == currentId) {
            isPlaying = MyMediaPlayer.readOrPause()
        } else {
            currentId = id
            MyMediaPlayer.stopMusic()
            MyMediaPlayer.startMusic(context, id)
            isPlaying = true
        }
        musics[position].isCurrentlyPlayed = isPlaying
        notifyDataSetChanged()
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val title: TextView = mView.title_list
        val artist: TextView = mView.artist_list
        val playPauseShrink: ImageButton = mView.play_pause_shrink
        val musicBackground: FrameLayout = mView.music_in_list_layout
    }
}

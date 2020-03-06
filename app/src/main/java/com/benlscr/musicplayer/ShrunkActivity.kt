package com.benlscr.musicplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.benlscr.musicplayer.databinding.ActivityShrunkBinding
import com.benlscr.musicplayer.dummy.DummyContent

class ShrunkActivity : AppCompatActivity(), MusicsFragment.OnListFragmentInteractionListener {

    private lateinit var binding: ActivityShrunkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShrunkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openExpandActivity()
        addMusicsFragment()
    }

    private fun openExpandActivity() {
        binding.expand.setOnClickListener {
            val intent = Intent(this, ExpandActivity::class.java)
            startActivity(intent)
        }
    }

    private fun addMusicsFragment() {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.musics_container, MusicsFragment.newInstance(1))
        fragmentTransaction.commit()
    }

    override fun onListFragmentInteraction(item: DummyContent.DummyItem?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

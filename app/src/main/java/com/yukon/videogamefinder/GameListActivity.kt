package com.yukon.videogamefinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.appbar.MaterialToolbar

class GameListActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_list)

        val toolbar : MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "0 results"

        // remove after completing design
        findViewById<View>(R.id.recyclerview_game_grid).setOnClickListener(this)
        findViewById<View>(R.id.recyclerview_game_list).setOnClickListener(this)
    }

    // remove after completing design
    override fun onClick(view: View?) {
        startActivity(Intent(this, GameDetailActivity::class.java))
    }

}


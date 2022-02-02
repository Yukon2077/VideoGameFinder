package com.yukon.videogamefinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<MaterialButton>(R.id.search_button).setOnClickListener(this)
                 
    }

    override fun onClick(view : View?) {
        val intent = Intent(this, GameListActivity::class.java)
        startActivity(intent)
    }

}
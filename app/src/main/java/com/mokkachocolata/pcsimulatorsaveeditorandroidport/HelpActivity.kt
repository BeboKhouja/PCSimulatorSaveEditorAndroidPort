package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager


class HelpActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_help, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        recyclerView.layoutManager = layoutManager

        val data = ArrayList<Url>()

        data.add(Url(resources.getString(R.string.about), 0, "file:///android_asset/About.htm"))
        data.add(Url(resources.getString(R.string.term), 1, "file:///android_asset/Terminologies.htm"))
        data.add(Url(resources.getString(R.string.save_file_help), 2, "file:///android_asset/PC Simulator Save Files.htm"))
        data.add(Url(resources.getString(R.string.websites), 3, "file:///android_asset/PC Simulator Websites.htm"))
        data.add(Url(resources.getString(R.string.sourceintro), 4, "file:///android_asset/Source/PC Simulator source code introduction.htm"))
        data.add(Url("Yiming.AntiCheat", 5, "file:///android_asset/Source/Yiming.AntiCheat.htm"))
        data.add(Url(resources.getString(R.string.howtouse), 6, "file:///android_asset/How to use Android port.htm"))
        val adapter = HelpRecyclerAdapter(data)

        recyclerView.adapter = adapter


    }

    override fun onClick(view: View?) {
        
    }
}
package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Arrays
import java.util.Scanner


public class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val savefiles = findViewById<Button>(R.id.savefiles)
        val about = findViewById<Button>(R.id.abouta)
        val term = findViewById<Button>(R.id.term)
        val websites = findViewById<Button>(R.id.websites)

        savefiles.setOnClickListener {
            startActivity(Intent(applicationContext, BrowserActivity::class.java).apply { putExtra("str", "file:///android_asset/PC Simulator Save Files.htm") })
        }

        about.setOnClickListener {
            startActivity(Intent(applicationContext, BrowserActivity::class.java).apply { putExtra("str", "file:///android_asset/About.htm") })
        }

        term.setOnClickListener {
            startActivity(Intent(applicationContext, BrowserActivity::class.java).apply { putExtra("str", "file:///android_asset/Terminologies.htm") })
        }

        websites.setOnClickListener {
            startActivity(Intent(applicationContext, BrowserActivity::class.java).apply { putExtra("str", "file:///android_asset/PC Simulator Websites.htm") })
        }

    }
}
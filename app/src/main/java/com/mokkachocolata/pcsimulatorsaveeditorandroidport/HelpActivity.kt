package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


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
        val about = findViewById<Button>(R.id.about)
        val term = findViewById<Button>(R.id.term)
        val websites = findViewById<Button>(R.id.websites)
        val sourceintro = findViewById<Button>(R.id.sourceintro)
        val yiminganticheat = findViewById<Button>(R.id.sourceyiminganticheat)

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

        sourceintro.setOnClickListener {
            startActivity(Intent(applicationContext, BrowserActivity::class.java).apply { putExtra("str", "file:///android_asset/Source/PC Simulator source code introduction.htm") })
        }

        yiminganticheat.setOnClickListener {
            startActivity(Intent(applicationContext, BrowserActivity::class.java).apply { putExtra("str", "file:///android_asset/Source/Yiming.AntiCheat.htm") })
        }

    }
}
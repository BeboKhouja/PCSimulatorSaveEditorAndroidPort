package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BrowserActivity : AppCompatActivity() {
    lateinit var browser: WebView
    override fun onDestroy(){
        super.onDestroy()
        browser.destroy()
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
        setContentView(R.layout.activity_browser)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        browser = findViewById(R.id.view)

        val maxIndex = 6
        var currentIndex = intent.getIntExtra("index",0)

        val settings = browser.settings
        settings.javaScriptEnabled = true

        data class Url(val name: String, val index: Int, val url: String)

        val urlArray = listOf(
            Url(resources.getString(R.string.about),0, "file:///android_asset/About.htm"),
            Url(resources.getString(R.string.term), 1, "file:///android_asset/Terminologies.htm"),
            Url(resources.getString(R.string.save_file_help), 2, "file:///android_asset/PC Simulator Save Files.htm"),
            Url(resources.getString(R.string.websites), 3, "file:///android_asset/PC Simulator Websites.htm"),
            Url(resources.getString(R.string.sourceintro), 4, "file:///android_asset/Source/PC Simulator source code introduction.htm"),
            Url("Yiming.AntiCheat", 5, "file:///android_asset/Source/Yiming.AntiCheat.htm"),
            Url(resources.getString(R.string.howtouse), 6, "file:///android_asset/How to use Android port.htm")
        )

        val forwardFAB = findViewById<FloatingActionButton>(R.id.nextFAB)
        val backFAB = findViewById<FloatingActionButton>(R.id.back)
        val content = findViewById<TextView>(R.id.content)

        backFAB.setOnClickListener { _ ->
            if (currentIndex > 0) {
                currentIndex--
                browser.loadUrl(urlArray[currentIndex].url)
                content.text = urlArray[currentIndex].name
                if (currentIndex == 0) {
                    backFAB.isEnabled = false
                }
                if (currentIndex == (maxIndex - 1)) {
                    forwardFAB.isEnabled = true
                }
            }
        }

        forwardFAB.setOnClickListener { _ ->
            if (currentIndex < maxIndex) {
                currentIndex++
                browser.loadUrl(urlArray[currentIndex].url)
                content.text = urlArray[currentIndex].name
                if (currentIndex == 1) {
                    backFAB.isEnabled = true
                }
                if (currentIndex == maxIndex) {
                    forwardFAB.isEnabled = false
                }
            }
        }
        browser.loadUrl(urlArray[currentIndex].url)
        content.text = urlArray[currentIndex].name
        content.bringToFront()
        if (currentIndex == 0) {
            backFAB.isEnabled = false
        }
        if (currentIndex == maxIndex) {
            forwardFAB.isEnabled = false
        }


    }
}
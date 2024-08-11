package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.util.Scanner

class BrowserActivity : AppCompatActivity() {
    lateinit var browser: WebView
    override fun onDestroy(){
        super.onDestroy()
        browser.destroy()
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

        browser = findViewById(R.id.view)

        val settings = browser.settings
        settings.javaScriptEnabled = true

        intent.getStringExtra("str")?.let { browser.loadUrl(it) }

    }
}
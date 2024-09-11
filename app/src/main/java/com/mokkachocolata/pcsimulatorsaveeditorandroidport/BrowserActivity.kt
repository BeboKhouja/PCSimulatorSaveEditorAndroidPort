/**
 * PC Simulator Save Editor is a free and open source save editor for PC Simulator.
 *     Copyright (C) 2024  Mokka Chocolata
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * Email: mokkachocolata@gmail.com
 */

package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.webkit.WebView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BrowserActivity : AppCompatActivity() {
    lateinit var forwardFAB: FloatingActionButton
    lateinit var backFAB: FloatingActionButton
    lateinit var browser: WebView
    private lateinit var globalVars : GlobalVars
    override fun onDestroy(){
        super.onDestroy()
        browser.destroy()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (event?.isAltPressed == true) {
                    backFAB.performClick()
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (event?.isAltPressed == true) {
                    forwardFAB.performClick()
                }
            }
        }
        return super.onKeyUp(keyCode, event)
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
        forwardFAB = findViewById(R.id.back)
        backFAB = findViewById(R.id.nextFAB)
        globalVars = GlobalVars(resources)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        browser = findViewById(R.id.view)

        val maxIndex = globalVars.urlArrayArray.size - 1
        var currentIndex = intent.getIntExtra("index",0)

        val settings = browser.settings
        settings.javaScriptEnabled = true

        val urlArray = globalVars.urlArray
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
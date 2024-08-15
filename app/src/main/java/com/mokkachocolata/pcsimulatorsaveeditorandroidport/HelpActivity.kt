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


class HelpActivity : AppCompatActivity(), View.OnClickListener {
    data class HelpButton(val button: Button, val index: Int)

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

    lateinit var helpButtons : List<HelpButton>
    override fun onClick(view: View) {
        Log.d("App", "clicked")
        for (button in helpButtons) {
            if (view == button.button) {
                val intent = Intent(this, BrowserActivity::class.java)
                intent.putExtra("index", button.index)
                startActivity(intent)
                break
            }
        }
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

        helpButtons = listOf(
            HelpButton(findViewById(R.id.about), 0),
            HelpButton(findViewById(R.id.term), 1),
            HelpButton(findViewById(R.id.savefiles), 2),
            HelpButton(findViewById(R.id.websites), 3),
            HelpButton(findViewById(R.id.sourceintro), 4),
            HelpButton(findViewById(R.id.sourceyiminganticheat), 5),
            HelpButton(findViewById(R.id.howtouse), 6)
        )

        for (button in helpButtons) {
            button.button.setOnClickListener(this)
        }


    }
}
package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ChangeLogActivity : AppCompatActivity() {
    val version = MainActivity2().version
    val ChangelogText = """
            PC Simulator Save Editor Android Port (version $version):
            A version of the PC Simulator Save Editor ported to Android.
            
            Changes:
            - Changed UI layout compatibility.
            - Added a menu bar.
            - Added a changelog.
        """.trimIndent()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_log)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textView = findViewById<TextView>(R.id.textView)

        textView.text = ChangelogText
    }
}
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
            - Added a search bar into the help activity (though this does not do anything.)
            - Added the back and forward buttons to the browser activity.
            - Added the title of the page in the browser activity.
            - Moved the help button to the action bar.
            - A translation error is fixed.
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
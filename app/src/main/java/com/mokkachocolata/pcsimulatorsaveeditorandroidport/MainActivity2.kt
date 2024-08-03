package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mokkachocolata.library.pcsimsaveeditor.MainFunctions


class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var decryptencrypt = findViewById<Button>(R.id.decryptencrypt)
        var input = findViewById<EditText>(R.id.inputText)
        var functions = MainFunctions()
        decryptencrypt.setOnClickListener {
            input.setText(functions.Decrypt(input.text.toString()))
        }

        var help = findViewById<Button>(R.id.help)

        help.setOnClickListener{
            functions.startHelpServer(17277)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(functions.helpServerURL))
            startActivity(browserIntent)

        }

    }
}
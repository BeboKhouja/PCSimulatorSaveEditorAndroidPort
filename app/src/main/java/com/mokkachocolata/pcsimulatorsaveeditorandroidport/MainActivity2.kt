package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
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
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader


class MainActivity2 : AppCompatActivity() {

    private val openFile = 0
    private val saveFile = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val decrypt = findViewById<Button>(R.id.decryptencrypt)
        val input = findViewById<EditText>(R.id.inputText)
        val functions = MainFunctions()
        decrypt.setOnClickListener {
            if (input.text.toString() != "") {
                input.setText(functions.Decrypt(input.text.toString()))
            }
        }

        val help = findViewById<Button>(R.id.help)

        help.setOnClickListener{
            startActivity(Intent(applicationContext, HelpActivity::class.java))

        }

        val open = findViewById<Button>(R.id.open)
        val save = findViewById<Button>(R.id.save)
        val copy = findViewById<Button>(R.id.clipboard)
        val chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
        }
        val saveIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }


        open.setOnClickListener{
            startActivityForResult(chooseFile, openFile)
        }

        save.setOnClickListener{
            if (input.text.toString() != "") {
                input.setText(functions.Decrypt(input.text.toString()))
                startActivityForResult(saveIntent, saveFile)
            }
        }

        copy.setOnClickListener{
            if (input.text.toString() != "") {
                val clipboard : ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("PC Simulator Save", input.text.toString())
                clipboard.setPrimaryClip(clip)
            }
        }

    }

    private fun readTextFromUri(uri: Uri): String {
        val uriThread = ReadTextFromUriThread()
        uriThread.resolver = contentResolver
        uriThread.uri = uri
        val actualThread = Thread(uriThread)
        actualThread.start()
        actualThread.join()
        return uriThread.output
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val functions = MainFunctions()
        val input = findViewById<EditText>(R.id.inputText)
        if (requestCode == openFile && resultCode == Activity.RESULT_OK) {
            data?.data?.also {uri ->
                val thread = Thread(functions)
                functions.input = readTextFromUri(uri)
                thread.start()
                thread.join()
                input.setText(functions.Output)
            }
        } else if (requestCode == saveFile && resultCode == Activity.RESULT_OK) {
            data?.data?.also {uri ->
                try {
                    contentResolver.openFileDescriptor(uri, "w")?.use { it ->
                        FileOutputStream(it.fileDescriptor).use {
                            it.write(input.text.toString().toByteArray())
                        }
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}

class ReadTextFromUriThread : Runnable {

    lateinit var uri : Uri
    var output = ""
    lateinit var resolver : ContentResolver

    override fun run() {
        val stringBuilder = StringBuilder()
        resolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        output = stringBuilder.toString()
    }

}
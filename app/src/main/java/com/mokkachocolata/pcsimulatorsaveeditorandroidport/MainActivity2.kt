package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
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
import kotlin.properties.Delegates


class MainActivity2 : AppCompatActivity() {

    private val openFile = 0
    private val saveFile = 1
    private var decrypt_after_opening = true
    private var encrypt_after_saving = true
    val version = "1.2.5"

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.decrypt_after_opening) {
            decrypt_after_opening = decrypt_after_opening.not()
        } else if (item.itemId == R.id.encrypt_after_saving) {
            encrypt_after_saving = encrypt_after_saving.not()
        } else if (item.itemId == R.id.changelog) {
            startActivity(Intent(applicationContext, ChangeLogActivity::class.java))
        } else if (item.itemId == R.id.about) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setMessage("""
                    |Created by Mokka Chocolata.
                    |Free, and open source.
                    |""".trimMargin())
                .setTitle("PC Simulator Save Editor Android Port (version $version)")
                .setPositiveButton("OK", null)

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

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
        decrypt.setOnClickListener { _ ->
            System.gc()
            if (input.text.toString() != "") {
                input.setText(functions.Decrypt(input.text.toString()))
            }
        }

        val help = findViewById<Button>(R.id.help)

        help.setOnClickListener {_ ->
            System.gc()
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


        open.setOnClickListener {_ ->
            System.gc()
            startActivityForResult(chooseFile, openFile)
        }

        save.setOnClickListener {_ ->
            System.gc()
            if (input.text.toString() != "") {
                if (encrypt_after_saving) {
                    input.setText(functions.Decrypt(input.text.toString()))
                }
                startActivityForResult(saveIntent, saveFile)
            }
        }

        copy.setOnClickListener{_ ->
            System.gc()
            if (input.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("PC Simulator Save", input.text.toString())
                clipboard.setPrimaryClip(clip)
            }
        }

    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val input = findViewById<EditText>(R.id.inputText)
        val writeorread = WriteOrReadThread()

        if (requestCode == openFile && resultCode == Activity.RESULT_OK) {
            val writeOrReadThread = Thread(writeorread)
           writeorread.resolver = contentResolver
            if (data != null) {
                writeorread.data = data
            }
           writeorread.input = input
           writeorread.decrypt_after_opening = decrypt_after_opening
           writeorread.encrypt_after_saving = encrypt_after_saving
           writeorread.WriteOrRead = false
            writeOrReadThread.start()
            writeOrReadThread.join()
        } else if (requestCode == saveFile && resultCode == Activity.RESULT_OK) {
            val writeOrReadThread = Thread(writeorread)
            writeorread.resolver = contentResolver
            if (data != null) {
                writeorread.data = data
            }
            writeorread.input = input
            writeorread.decrypt_after_opening = decrypt_after_opening
            writeorread.encrypt_after_saving = encrypt_after_saving
            writeorread.WriteOrRead = true
            writeOrReadThread.start()
            writeOrReadThread.join()
        }
    }
}

class ReadTextFromUriThread : Runnable {

    lateinit var uri : Uri
    var output = ""
    lateinit var resolver : ContentResolver

    override fun run() {
        val stringBuilder = StringBuilder()
        val inputStream = resolver.openInputStream(uri)

        inputStream?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        inputStream?.close()
        output = stringBuilder.toString()
        stringBuilder.clear()
    }

}

class WriteOrReadThread(): Runnable{
    var WriteOrRead by Delegates.notNull<Boolean>()
    lateinit var input : EditText
    lateinit var data : Intent
    lateinit var resolver : ContentResolver
    var functions = MainFunctions()
    var decrypt_after_opening by Delegates.notNull<Boolean>()
    var encrypt_after_saving by Delegates.notNull<Boolean>()

    private fun readTextFromUri(uri: Uri): String {
        val uriThread = ReadTextFromUriThread()
        uriThread.resolver = resolver
        uriThread.uri = uri
        val actualThread = Thread(uriThread)
        actualThread.start()
        actualThread.join()
        return uriThread.output
    }

    override fun run() {
        if (!WriteOrRead) {
            data?.data?.also {uri ->
                val thread = Thread(functions)
                functions.input = readTextFromUri(uri)
                thread.start()
                thread.join()
                if (decrypt_after_opening) {
                    input.setText(functions.Output)
                } else {
                    input.setText(functions.Decrypt(functions.Output))
                }
            }
        } else {
            data?.data?.also {uri ->
                try {
                    resolver.openFileDescriptor(uri, "w")?.use { it ->
                        val outputstream = FileOutputStream(it.fileDescriptor)

                        outputstream.use {
                            it.write(input.text.toString().toByteArray())
                        }
                        outputstream.close()
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
package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mokkachocolata.library.pcsimsaveeditor.MainFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class MainActivity2 : AppCompatActivity() {

    lateinit var text : String
    private val openFile = 0
    var done = false
    private val saveFile = 1
    val openFileAndSaveToTxt = 2
    val saveToTxtInClass = 3
    private var decrypt_after_opening = true
    private var encrypt_after_saving = true
    val version = "1.3.2"
    lateinit var input : EditText
    lateinit var save : Button
    lateinit var saveIntent: Intent
    val ChangelogText = """
            - Fixed a crash when the app is switched or launched in light mode. (apparently the problem is caused by a conflicting theme.)
            - Corrected the info button in light mode.
        """.trimIndent()


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.decrypt_after_opening) {
            decrypt_after_opening = decrypt_after_opening.not()
        } else if (item.itemId == R.id.encrypt_after_saving) {
            encrypt_after_saving = encrypt_after_saving.not()
        } else if (item.itemId == R.id.changelog) {
            if (Build.VERSION.SDK_INT >= 31) {
                val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
                builder
                    .setIcon(R.drawable.baseline_info_outline_24)
                    .setMessage(ChangelogText)
                    .setTitle("Changelog (version $version)")
                    .setPositiveButton("Ok", null)
                    .show()
            } else {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder
                    .setIcon(R.drawable.baseline_info_outline_24)
                    .setMessage(ChangelogText)
                    .setTitle("Changelog (version $version)")
                    .setPositiveButton("Ok", null)

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        } else if (item.itemId == R.id.about) {
            if (Build.VERSION.SDK_INT >= 31) {
                val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
                builder
                    .setIcon(R.drawable.baseline_info_outline_24)
                    .setMessage(
                        """
                    |Created by Mokka Chocolata.
                    |Free, and open source.
                    |""".trimMargin()
                    )
                    .setTitle("PC Simulator Save Editor Android Port (version $version)")
                    .setPositiveButton("Ok", null)
                    .show()
            } else {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder
                    .setIcon(R.drawable.baseline_info_outline_24)
                    .setMessage("""
                    |Created by Mokka Chocolata.
                    |Free, and open source.
                    |""".trimMargin())
                    .setTitle("PC Simulator Save Editor Android Port (version $version)")
                    .setPositiveButton("Ok", null)

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

        } else if (item.itemId == R.id.help) {
            System.gc()
            startActivity(Intent(applicationContext, HelpActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        input.text.clear()
        System.gc()
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
        DynamicColors.applyToActivitiesIfAvailable(application)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        input = findViewById(R.id.inputText)

        val decrypt = findViewById<Button>(R.id.decryptencrypt)

        val functions = MainFunctions()
        decrypt.setOnClickListener { _ ->
            System.gc()
            if (input.text.toString() != "") {
                input.setText(functions.Decrypt(input.text.toString()))
            }
        }

        val open = findViewById<Button>(R.id.open)
        save = findViewById(R.id.save)
        val copy = findViewById<Button>(R.id.clipboard)
        val chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
        }
        saveIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
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

        val decryptToTxt = findViewById<Button>(R.id.decryptToTxt)

        decryptToTxt.setOnClickListener { _ ->
            System.gc()
            startActivityForResult(chooseFile, openFileAndSaveToTxt)
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
        val afterread = AfterReadThread()

        if (requestCode == saveToTxtInClass && resultCode == Activity.RESULT_OK) {
            Log.i("App","Reached here")
            val afterReadThread = Thread(afterread)
            afterread.resolver = contentResolver
            if (data != null) {
                afterread.afterData = data
            }
            Log.d("App", text)
            afterread.text = text
            afterReadThread.start()
            afterReadThread.join()

        } else if (requestCode == openFile && resultCode == Activity.RESULT_OK) {
            val writeOrReadThread = Thread(writeorread)
            writeorread.clazz = this
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
            System.gc()
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
            System.gc()
        } else if (requestCode == openFileAndSaveToTxt && resultCode == Activity.RESULT_OK) {
            val writeOrReadThread = Thread(writeorread)
            writeorread.clazz = this
            writeorread.resolver = contentResolver
            if (data != null) {
                writeorread.data = data
            }
            writeorread.input = input
            writeorread.decrypt_after_opening = decrypt_after_opening
            writeorread.encrypt_after_saving = encrypt_after_saving
            writeorread.WriteOrRead = true
            writeorread.saveToTxt = true
            writeorread.doClazz = afterread
            writeOrReadThread.start()
            writeOrReadThread.join()
            System.gc()
        }
    }
}

class ReadTextFromUriThread : Runnable {

    lateinit var uri : Uri
    var output = ""
    lateinit var resolver : ContentResolver

    override fun run() {
        System.gc()
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
        System.gc()
    }

}

class WriteOrReadThread(): Runnable{
    var WriteOrRead by Delegates.notNull<Boolean>()
    lateinit var input : EditText
    lateinit var data : Intent
    lateinit var resolver : ContentResolver
    var saveToTxt by Delegates.notNull<Boolean>()
    var functions = MainFunctions()
    var decrypt_after_opening by Delegates.notNull<Boolean>()
    var encrypt_after_saving by Delegates.notNull<Boolean>()
    lateinit var clazz : MainActivity2
    lateinit var doClazz : AfterReadThread

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
            }
        } else if (saveToTxt) {
            data?.data?.also {uri ->
                val thread = Thread(functions)
                functions.input = readTextFromUri(uri)
                thread.start()
                thread.join()
                Log.d("App", functions.Output)
                clazz.text = functions.Output
                clazz.startActivityForResult(clazz.saveIntent, clazz.saveToTxtInClass)
            }
        } }
    }

class AfterReadThread() : Runnable{
    lateinit var afterData : Intent
    lateinit var resolver : ContentResolver
    lateinit var text: String
    var functions = MainFunctions()

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
        afterData?.data?.also {uri ->
            try {
                resolver.openFileDescriptor(uri, "w")?.use { it ->
                    val outputstream = FileOutputStream(it.fileDescriptor)

                    outputstream.use {
                        it.write(text.toByteArray())
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
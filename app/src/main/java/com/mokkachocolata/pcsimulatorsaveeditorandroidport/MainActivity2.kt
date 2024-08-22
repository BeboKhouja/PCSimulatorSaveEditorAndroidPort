package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.DragEvent.ACTION_DROP
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mokkachocolata.library.pcsimsaveeditor.MainFunctions
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Integer.parseInt
import kotlin.properties.Delegates

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

class MainActivity2 : AppCompatActivity() {

    private lateinit var globalVars : GlobalVars
    lateinit var text : String
    private val openFile = 0
    private val saveFile = 1
    private val openFileAndSaveToTxt = 2
    val saveToTxtInClass = 3
    private var decrypt_after_opening = true
    private var encrypt_after_saving = true
    private lateinit var version : String
    private lateinit var input : EditText
    lateinit var save : Button
    lateinit var saveIntent: Intent
    private lateinit var ChangelogText : String

    fun dialog(title: String, message: String, okListener: OnClickListener?, cancelListener: OnClickListener?) {
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                .setMessage(message)
                .setTitle(title)
            if (okListener != null) {
                builder.setPositiveButton("Ok", okListener)
            }
            if (cancelListener != null) {
                builder.setNegativeButton("Cancel", cancelListener)
            }
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                .setMessage(message)
                .setTitle(title)
            if (okListener != null) {
                builder.setPositiveButton("Ok", okListener)
            }
            if (cancelListener != null) {
                builder.setNegativeButton("Cancel", cancelListener)
            }

            builder.show()
        }
    }

    fun dialog(title: String,
               message: String?,
               okListener: OnClickListener?,
               cancelListener: OnClickListener?,
               adapter : Array<String>,
               adapterOnClickListener: OnClickListener
    ) {
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                .setMessage(message)
                .setTitle(title)
                .setItems(adapter, adapterOnClickListener)
            if (okListener != null) {
                builder.setPositiveButton("Ok", okListener)
            }
            if (cancelListener != null) {
                builder.setNegativeButton("Cancel", cancelListener)
            }
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                if (message != null) {
                    builder.setMessage(message)
                }
                builder
                    .setTitle(title)
                    .setItems(adapter, adapterOnClickListener)
            if (okListener != null) {
                builder.setPositiveButton("Ok", okListener)
            }
            if (cancelListener != null) {
                builder.setNegativeButton("Cancel", cancelListener)
            }
            builder.show()
        }
    }

    fun dialog(title: String,
               message: String?,
               okListener: OnClickListener?,
               cancelListener: OnClickListener?,
               view : View
    ) {
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                .setMessage(message)
                .setTitle(title)
                .setView(view)
            if (okListener != null) {
                builder.setPositiveButton("Ok", okListener)
            }
            if (cancelListener != null) {
                builder.setNegativeButton("Cancel", cancelListener)
            }
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
            if (message != null) {
                builder.setMessage(message)
            }
            builder
                .setTitle(title)
                .setView(view)
            if (okListener != null) {
                builder.setPositiveButton("Ok", okListener)
            }
            if (cancelListener != null) {
                builder.setNegativeButton("Cancel", cancelListener)
            }
            builder.show()
        }
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.decrypt_after_opening) {
            decrypt_after_opening = decrypt_after_opening.not()
        } else if (item.itemId == R.id.encrypt_after_saving) {
            encrypt_after_saving = encrypt_after_saving.not()
        } else if (item.itemId == R.id.changelog) {
            dialog("Changelog (version $version)", globalVars.ChangelogText, null, null)
        } else if (item.itemId == R.id.about) {
            dialog("PC Simulator Save Editor Android Port (version $version)", """
                    |Created by Mokka Chocolata.
                    |Free, and open source.
                    |""".trimMargin(), null, null
            )
        } else if (item.itemId == R.id.help) {
            System.gc()
            startActivity(Intent(applicationContext, HelpActivity::class.java))
        } else if (item.itemId == R.id.discord) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/GXRECJjhVr")))
        } else if (item.itemId == R.id.insert) {
            val itemList = arrayOf(
                "RTX4080Ti",
                "Pillow",
                "Cube",
                "Projector",
            )
            dialog(resources.getString(R.string.insert), null, null, null, itemList) { _, i ->
                val text = input.text.toString()
                val jsonObject = JSONObject(text.lines()[1])
                Log.d("App", jsonObject.toString())
                val itemArray = jsonObject.getJSONArray("itemData")
                val position = Position(
                    (jsonObject.get("playerData") as JSONObject).getDouble("x"),
                    (jsonObject.get("playerData") as JSONObject).getDouble("y"),
                    (jsonObject.get("playerData") as JSONObject).getDouble("z")
                )
                val obj = ObjectJson(itemList[i], (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0))
                itemArray.put(obj.toJson())
                val lines = text.lines()
                input.setText(lines[lines.size - 1] + "\n" + jsonObject.toString())
            }
        } else if (item.itemId == R.id.saveoptions) {
            val optionList = arrayOf(
                "AC Temperature",
                "AC Power",
                "Version",
                "Money",
                "Room",
                "Gravity",
                "Hardcore",
                "Playtime",
                "Light",
                "Sign",
                "Save name"
            )
            val text = input.text.toString()
            lateinit var jsonObject : JSONObject
            val init = text.lines()[0].isNotEmpty()
            if (text.lines()[0].isNotEmpty()) {
                jsonObject = JSONObject(text.lines()[0])
            }
            dialog("Save Options", null, null, null, optionList) {_, i ->
                when (i) {
                    0 -> {
                        val edittext = EditText(this)
                        edittext.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                        dialog("AC Temperature", "Set the temperature of the AC. Max 2147483647 and min -2147483648" ,{_, i ->
                            if (init) {
                                jsonObject.put("temperature", parseInt(edittext.text.toString()))
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, edittext)
                    }

                    1 -> {
                        val checkbox = Switch(this)
                        checkbox.text = "Power"
                        dialog("AC Power", "Power on or off the AC." ,{_, i ->
                            if (init) {
                                jsonObject.put("ac", checkbox.isChecked)
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, checkbox)
                    }
                    2 -> {
                        val edittext = EditText(this)
                        dialog("Version", "Set the version of the save." ,{_, i ->
                            if (init) {
                                jsonObject.put("version", edittext.text)
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, edittext)
                    }
                    3 -> {
                        val edittext = EditText(this)
                        edittext.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                        dialog("Money", "Set the money of the save. Max 2147483647 and min -2147483647" ,{_, i ->
                            if (init) {
                                jsonObject.put("coin", parseInt(edittext.text.toString()))
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, edittext)
                    }

                    4 -> {
                        val edittext = EditText(this)
                        edittext.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                        dialog("Room", "Set the current room.\n0: Medium\n1: Large\n2: Double Storey\n3: Factory." ,{_, i ->
                            if (init) {
                                jsonObject.put("room", parseInt(edittext.text.toString()))
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, edittext)
                    }

                    5 -> {
                        val checkbox = Switch(this)
                        checkbox.text = "Gravity"
                        dialog("Gravity", "Switch on or off gravity." ,{_, i ->
                            if (init) {
                                jsonObject.put("gravity", checkbox.isChecked)
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, checkbox)
                    }

                    6 -> {
                        val checkbox = Switch(this)
                        checkbox.text = "Hardcore"
                        dialog("Hardcore", "Switch on or off hardcore mode." ,{_, i ->
                            if (init) {
                                jsonObject.put("hardcore", checkbox.isChecked)
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, checkbox)
                    }

                    7 -> {
                        val edittext = EditText(this)
                        edittext.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                        dialog("Playtime", "Set the current playtime." ,{_, i ->
                            if (init) {
                                jsonObject.put("playtime", parseInt(edittext.text.toString()))
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, edittext)
                    }

                    8 -> {
                        val checkbox = Switch(this)
                        checkbox.text = "Light"
                        dialog("Light", "Switch on or off the lamp." ,{_, i ->
                            if (init) {
                                jsonObject.put("light", checkbox.isChecked)
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, checkbox)
                    }
                    9 -> {
                        val edittext = EditText(this)
                        dialog("Sign", "Set the signer of the save." ,{_, i ->
                            if (init) {
                                jsonObject.put("sign", edittext.text)
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, edittext)
                    }
                    10 -> {
                        val edittext = EditText(this)
                        dialog("Save name", "Set the name of the save." ,{_, i ->
                            if (init) {
                                jsonObject.put("roomName", edittext.text)
                                input.setText(jsonObject.toString() + "\n" + text.lines()[1])
                            }
                        } , {_, _ ->}, edittext)
                    }
                }
            }
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
        if (Build.VERSION.SDK_INT >= 31){
            installSplashScreen()
        }
        globalVars = GlobalVars(resources)
        ChangelogText = globalVars.ChangelogText
        version = globalVars.version
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

        fun readTextFromUri(uri: Uri): String {
            val uriThread = ReadTextFromUriThread()
            uriThread.resolver = contentResolver
            uriThread.uri = uri
            val actualThread = Thread(uriThread)
            actualThread.start()
            actualThread.join()
            return uriThread.output
        }

        if (Build.VERSION.SDK_INT > 24) {
            input.setOnDragListener{view, event ->
                when (event.action) {
                    ACTION_DROP -> {
                        // The app sometimes crashes whenever the dragged content goes into the app. This is not our fault, it's Android's fault.

                        val fileItem: ClipData.Item = event.clipData.getItemAt(0)
                        val uri : Uri? = fileItem.uri
                        // The complicated way to check if a Uri is a file or not
                        val scheme = uri?.let { contentResolver.getType(it) } != "vnd.android.document/directory" || contentResolver.getType(uri) == null
                        val dropPermissions = requestDragAndDropPermissions(event)
                        uri?.let {
                            if (scheme) {
                                (view as EditText).setText(readTextFromUri(it))
                                dropPermissions.release()
                            } else {
                                val snack = Snackbar.make(findViewById(R.id.main), "Not a file, won't decrypt.", Snackbar.LENGTH_SHORT)
                                snack.show()
                            }
                        } ?: run {
                            return@setOnDragListener false
                        }

                        return@setOnDragListener true

                    }

                    else -> {
                        return@setOnDragListener false
                    }
                }
            }
        }


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
            type = "application/octet-stream"
        }
        saveIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "application/octet-stream"
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

        val appLinkIntent: Intent = intent
        val appLinkAction: String? = appLinkIntent.action
        val appLinkData: Uri? = appLinkIntent.data
        if (appLinkAction != null) {
            Log.d("App", appLinkAction)
        }
        Log.d("App", appLinkData.toString())
        if (Intent.ACTION_VIEW == appLinkAction) {
            appLinkData?.lastPathSegment?.also { _ ->
                val uriThread = ReadTextFromUriThread()
                val uri = Thread(uriThread)
                uriThread.uri = appLinkData
                uriThread.resolver = contentResolver
                uri.start()
                uri.join()
                System.gc()
                if (decrypt_after_opening) {
                    input.setText(functions.Decrypt(uriThread.output))
                } else {
                    input.setText(uriThread.output)
                }
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
            val afterReadThread = Thread(afterread)
            afterread.resolver = contentResolver
            if (data != null) {
                afterread.afterData = data
            }
            afterread.text = input.text.toString()
            afterReadThread.start()
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


class WriteOrReadThread : Runnable{
    var WriteOrRead by Delegates.notNull<Boolean>()
    lateinit var input : EditText
    lateinit var data : Intent
    lateinit var resolver : ContentResolver
    var saveToTxt by Delegates.notNull<Boolean>()
    private var functions = MainFunctions()
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
                if (decrypt_after_opening) {
                    input.setText(functions.Output)
                } else {
                    input.setText(readTextFromUri(uri))
                }
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

class AfterReadThread : Runnable{
    lateinit var afterData : Intent
    lateinit var resolver : ContentResolver
    lateinit var text: String

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
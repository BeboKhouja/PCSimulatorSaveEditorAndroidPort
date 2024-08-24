package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.DragEvent.ACTION_DROP
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.mokkachocolata.library.pcsimsaveeditor.MainFunctions
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Integer.parseInt
import java.lang.Long.parseLong
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
    private lateinit var resolver : ContentResolver
    private var fileList = arrayOf(
        "App Manager",
        "Daily Market",
        "Personalization",
        "EZ Mining",
        "Benchmark",
        "File Manager",
        "Disk Management",
        "System Info",
        "Frequency Settings",
        "Paint",
        "RGB Controller",
        "Terminal",
        "Text Editor",
        "Video Player",
        "Animator",
        "My Devices",
        "Browser",
        "Boot File",
        "Virus"
    )
    @SuppressLint("SetTextI18n")
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

        if (uri != null) {
            val text = input.text.toString()
            val jsonObject = JSONObject(text.lines()[1])
            val itemArray = jsonObject.getJSONArray("itemData")
            val position = Position(
                (jsonObject.get("playerData") as JSONObject).getDouble("x"),
                (jsonObject.get("playerData") as JSONObject).getDouble("y"),
                (jsonObject.get("playerData") as JSONObject).getDouble("z")
            )
            val bm = BitmapFactory.decodeStream(resolver.openInputStream(uri))
            val baos = ByteArrayOutputStream()
            Thread {
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            }.apply {
                start()
                join()
            }
            val b = baos.toByteArray()
            lateinit var obj : BannerObjectJson
            Thread {
                obj = BannerObjectJson("BannerStand", (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), Base64.encodeToString(b, Base64.DEFAULT))
            }.apply {
                start()
                join()
            }
            itemArray.put(obj.toJson())
            val lines = text.lines()
            input.setText(lines[0] + "\n" + jsonObject.toString())
        }
    }

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

    fun dialogMultiChoice(title: String,
               message: String?,
               okListener: OnClickListener?,
               cancelListener: OnClickListener?,
               adapter : Array<String>,
               adapterOnClickListener: OnMultiChoiceClickListener,
                          boolArray: BooleanArray
    ) {
        val items = boolArray
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                .setMessage(message)
                .setTitle(title)
                .setMultiChoiceItems(adapter, items, adapterOnClickListener)
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
                .setMultiChoiceItems(adapter, items, adapterOnClickListener)
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




    @SuppressLint("SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.decrypt_after_opening -> {
                decrypt_after_opening = decrypt_after_opening.not()
            }
            R.id.encrypt_after_saving -> {
                encrypt_after_saving = encrypt_after_saving.not()
            }
            R.id.changelog -> {
                dialog("Changelog (version $version)", globalVars.ChangelogText, null, null)
            }
            R.id.about -> {
                dialog("PC Simulator Save Editor Android Port (version $version)", """
                        |Created by Mokka Chocolata.
                        |Free, and open source.
                        |Get beta builds at the Actions tab at the GitHub repository.
                        |""".trimMargin(), null, null
                )
            }
            R.id.help -> {
                System.gc()
                startActivity(Intent(applicationContext, HelpActivity::class.java))
            }
            R.id.discord -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/GXRECJjhVr")))
            }
            R.id.dump -> {
                val text = input.text.toString()
                val jsonObject = JSONObject(text.lines()[1])
                val itemArray = jsonObject.getJSONArray("itemData")
                var pwd = ""
                for (i in 0 until itemArray.length()) {
                    val storage = itemArray.getJSONObject(i)
                    val spawnId = storage.getString("spawnId")
                    if (spawnId.contains("SSD") or spawnId.contains("HDD") or spawnId.contains("SSD_M.2")) {
                        val data = storage.getJSONObject("data")
                        if (data.has("storageData")) {
                            val storageData = data.getJSONObject("storageData")
                            val password = storageData.getString("userPassword")
                            if (password.isNotEmpty()) {
                                pwd += "$password : $spawnId\n"
                            }
                        } else {
                            // Assume we use a different way to do this
                            val password = data.getString("password")
                            if (password.isNotEmpty()) {
                                pwd += "$password : $spawnId\n"
                            }
                        }
                    } else if (spawnId == "FlashDrive") {
                        // Flash Drive uses a different structure
                        val data = storage.getJSONObject("data")
                        if (data.has("storageData")) {
                            val storageData = data.getJSONObject("storageData")
                            val password = storageData.getString("userPassword")
                            if (password.isNotEmpty()) {
                                pwd += "$password : $spawnId\n"
                            }
                        } else {
                            // Assume we use a different way to do this
                            val password = data.getString("password")
                            if (password.isNotEmpty()) {
                                pwd += "$password : $spawnId\n"
                            }
                        }
                    }
                }
                dialog("Result", pwd, {_,_->}, null)
            }
            R.id.insert -> {
                val itemList = arrayOf(
                    "RTX4080Ti",
                    "Pillow",
                    "Cube",
                    "Projector",
                    "Banner",
                    "Flash Drive",
                    "SSD",
                    "M.2 NVMe SSD",
                    "HDD",
                    "Daily Market (bypass bitcoin requirement)",
                )
                dialog(resources.getString(R.string.insert), null, null, null, itemList) { _, i ->
                    fun getItemArray() : JSONArray {
                        val text = input.text.toString()
                        val jsonObject = JSONObject(text.lines()[1])
                        val itemArray = jsonObject.getJSONArray("itemData")
                        return itemArray
                    }
                    fun getPos() : Position {
                        val text = input.text.toString()
                        val jsonObject = JSONObject(text.lines()[1])
                        val position = Position(
                            (jsonObject.get("playerData") as JSONObject).getDouble("x"),
                            (jsonObject.get("playerData") as JSONObject).getDouble("y"),
                            (jsonObject.get("playerData") as JSONObject).getDouble("z")
                        )
                        return position
                    }
                    fun doit() {
                        val text = input.text.toString()
                        val jsonObject = JSONObject(text.lines()[1])
                        val itemArray = getItemArray()
                        val obj = ObjectJson(itemList[i], (0..2147483647).random(), getPos(), Rotation(0.0,0.0,0.0,0.0))
                        itemArray.put(obj.toJson())
                        val lines = text.lines()
                        input.setText(lines[0] + "\n" + jsonObject.toString())
                    }
                    fun doitBanner() {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    fun putFile(i: Int, array: JSONArray) {
                        when(i) {
                            0 -> {
                                val file = FileObjectJson("App Downloader.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            1 -> {
                                val file = FileObjectJson("Daily Market.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            2 -> {
                                val file = FileObjectJson("Personalization.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            3 -> {
                                val file = FileObjectJson("EZ Mining.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            4 -> {
                                val file = FileObjectJson("Benchmark.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            5 -> {
                                val file = FileObjectJson("File Manager.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            6 -> {
                                val file = FileObjectJson("Disk Management.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            7 -> {
                                val file = FileObjectJson("System Info.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            8 -> {
                                val file = FileObjectJson("Frequency Settings.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            9 -> {
                                val file = FileObjectJson("Paint.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            10 -> {
                                val file = FileObjectJson("RGB Controller.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            11 -> {
                                val file = FileObjectJson("Terminal.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            12 -> {
                                val file = FileObjectJson("Text Editor.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            13 -> {
                                val file = FileObjectJson("Video Player.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            14 -> {
                                val file = FileObjectJson("Animator.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            15 -> {
                                val file = FileObjectJson("My Devices.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            16 -> {
                                val file = FileObjectJson("Browser.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                            17 -> {
                                val file = FileObjectJson("System/boot.bin", "pcos", true, 0, 0)
                                array.put(file.toJson())
                            }
                            18 -> {
                                val file = FileObjectJson("Launcher.exe", "", false, 0, 0)
                                array.put(file.toJson())
                            }
                        }
                    }
                    when (i) {
                        0 -> {
                            doit()
                        }

                        1 -> {
                            doit()
                        }

                        2 -> {
                            doit()
                        }

                        3 -> {
                            doit()
                        }
                        4 -> {
                            doitBanner()
                        }

                        5 -> {
                            val text = input.text.toString()
                            val jsonObject = JSONObject(text.lines()[1])
                            val itemArray = jsonObject.getJSONArray("itemData")
                            val position = getPos()
                            val lines = text.lines()
                            var driveName : String
                            val boolArray = BooleanArray(fileList.size)

                            val selectedItems = mutableListOf(*fileList)
                            var password : String
                            val edittext = EditText(this)
                            dialog("USB Drive Name", "Set the storage name that appears in Disk Management and when you hold it.", { _, _ ->
                                driveName = edittext.text.toString()
                                val edittext = EditText(this)
                                edittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                dialog("Password", "Set the password of this drive.", { _, _ ->
                                    password = edittext.text.toString()
                                    // Null the message out because the items wont appear
                                    dialogMultiChoice("Files", null, {_, _ ->
                                        val array = JSONArray()
                                        for (i in boolArray.indices) {
                                            if (boolArray[i]) {
                                                putFile(i, array)
                                            }
                                        }
                                        val drive = USBObjectJson((0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array)
                                        itemArray.put(drive.toJson())
                                        input.setText(lines[0] + "\n" + jsonObject.toString())
                                    }, null, fileList, {_, which, isChecked ->
                                        boolArray[which] = isChecked
                                        val currentItem = selectedItems[which]
                                    }, boolArray)
                                }, {_,_->}, edittext)
                            }, {_,_->}, edittext)
                        }

                        6 -> {val text = input.text.toString()
                            val jsonObject = JSONObject(text.lines()[1])
                            val itemArray = jsonObject.getJSONArray("itemData")
                            val position = getPos()
                            val lines = text.lines()
                            var driveName: String

                            val boolArray = BooleanArray(fileList.size)

                            val selectedItems = mutableListOf(*fileList)
                            var password: String
                            var size : String
                            val edittext = EditText(this)
                            dialog("SSD Drive Name", "Set the storage name that appears in Disk Management and when you hold it.", { _, _ ->
                                driveName = edittext.text.toString()
                                val edittext = EditText(this)
                                edittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                dialog("Password", "Set the password of this drive.", { _, _ ->
                                    password = edittext.text.toString()
                                    // Null the message out because the items wont appear
                                    val ssdSize = arrayOf(
                                        "128GB",
                                        "256GB",
                                        "512GB",
                                        "1TB",
                                        "2TB",
                                    )
                                    dialog("SSD Size", null, null,  null, ssdSize) {_, i ->
                                        size = ssdSize[i]

                                        dialogMultiChoice("Files", null, {_, _ ->
                                            val array = JSONArray()
                                            for (i in boolArray.indices) {
                                                if (boolArray[i]) {
                                                    putFile(i, array)
                                                }
                                            }
                                            val drive = SSDObjectJson(size, (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array, "User")
                                            itemArray.put(drive.toJson())
                                            input.setText(lines[0] + "\n" + jsonObject.toString())
                                        }, null, fileList, {_, which, isChecked ->
                                            boolArray[which] = isChecked
                                            val currentItem = selectedItems[which]
                                        }, boolArray)
                                    }
                                }, {_,_->}, edittext)
                            }, {_,_->}, edittext)}

                        7 -> {val text = input.text.toString()
                            val jsonObject = JSONObject(text.lines()[1])
                            val itemArray = jsonObject.getJSONArray("itemData")
                            val lines = text.lines()
                            var driveName: String
                            val boolArray = BooleanArray(fileList.size)

                            val selectedItems = mutableListOf(*fileList)
                            var password: String
                            var size : String
                            val edittext = EditText(this)
                            dialog("M.2 NVMe SSD Drive Name", "Set the storage name that appears in Disk Management and when you hold it.", { _, _ ->
                                driveName = edittext.text.toString()
                                val edittext = EditText(this)
                                edittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                dialog("Password", "Set the password of this drive.", { _, _ ->
                                    password = edittext.text.toString()
                                    // Null the message out because the items wont appear
                                    val m2size = arrayOf(
                                        "128GB",
                                        "256GB",
                                        "512GB",
                                        "1TB",
                                    )
                                    dialog("M.2 NVMe SSD Size", null, null,  null, m2size) {_, i ->
                                        size = m2size[i]

                                        dialogMultiChoice("Files", null, {_, _ ->
                                            val array = JSONArray()
                                            for (i in boolArray.indices) {
                                                if (boolArray[i]) {
                                                    putFile(i, array)
                                                }
                                            }
                                            val drive = M2ObjectJson(size, (0..2147483647).random(), getPos(), Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array, "User")
                                            itemArray.put(drive.toJson())
                                            input.setText(lines[0] + "\n" + jsonObject.toString())
                                        }, null, fileList, {_, which, isChecked ->
                                            boolArray[which] = isChecked
                                            val currentItem = selectedItems[which]
                                        }, boolArray)
                                    }
                                }, {_,_->}, edittext)
                            }, {_,_->}, edittext)}

                        8 -> {val text = input.text.toString()
                            val jsonObject = JSONObject(text.lines()[1])
                            val itemArray = jsonObject.getJSONArray("itemData")
                            val lines = text.lines()
                            var driveName: String
                            val boolArray = BooleanArray(fileList.size)

                            val selectedItems = mutableListOf(*fileList)
                            var password: String
                            var size: String
                            val edittext = EditText(this)
                            dialog("HDD Drive Name", "Set the storage name that appears in Disk Management and when you hold it.", { _, _ ->
                                driveName = edittext.text.toString()
                                val edittext = EditText(this)
                                edittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                dialog("Password", "Set the password of this drive.", { _, _ ->
                                    password = edittext.text.toString()
                                    val edittext = EditText(this)
                                    // Null the message out because the items wont appear
                                    val hddsize = arrayOf(
                                        "500GB",
                                        "1TB",
                                        "2TB",
                                        "5TB",
                                    )
                                    dialog("HDD Size", null, null,  null, hddsize) {_, i ->
                                        size = hddsize[i]

                                        dialogMultiChoice("Files", null, {_, _ ->
                                            val array = JSONArray()
                                            for (i in boolArray.indices) {
                                                if (boolArray[i]) {
                                                    putFile(i, array)
                                                }
                                            }
                                            val drive = HDDObjectJson(size, (0..2147483647).random(), getPos(), Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array, "User")
                                            itemArray.put(drive.toJson())
                                            input.setText(lines[0] + "\n" + jsonObject.toString())
                                        }, null, fileList, {_, which, isChecked ->
                                            boolArray[which] = isChecked
                                            val currentItem = selectedItems[which]
                                        }, boolArray)
                                    }
                                }, {_,_->}, edittext)
                            }, {_,_->}, edittext)}

                        9 -> {
                            val text = input.text.toString()
                            val jsonObject = JSONObject(text.lines()[1])
                            val itemArray = jsonObject.getJSONArray("itemData")
                            var obj: ObjectJson
                            val lines = text.lines()
                            data class dailyObj(val name: String, val internalName: String)
                            val daily = arrayOf(
                                dailyObj("Apson A3", "Apson_A3"),
                                dailyObj("Bitcoin", "Bitcoin"),
                                dailyObj("Box Removal Bomb", "BoxRemovalBomb"),
                                dailyObj("Cooler", "Carrot"),
                                dailyObj("Chair", "Chair"),
                                dailyObj("Crate", "Crate"),
                                dailyObj("Gaming Chair (Blue)", "GamingChair"),
                                dailyObj("Gaming Chair (Purple)", "GamingChair_1"),
                                dailyObj("Gaming Chair (Red)", "GamingChair_2"),
                                dailyObj("Gaming Chair (White)", "GamingChair_2"),
                                dailyObj("Generator", "Generator"),
                                dailyObj("Bomb", "Gift"),
                                dailyObj("Hammer", "Hammer"),
                                dailyObj("Headphone", "Headphone"),
                                dailyObj("Headphone Stand", "HeadphoneStand"),
                                dailyObj("Holographic Projector", "HolographicProjector"),
                                dailyObj("Keyboard", "Keyboard"),
                                dailyObj("Laptop", "Laptop"),
                                dailyObj("Led Display (Clock)", "LedDisplay_Clock"),
                                dailyObj("Led Display (PC)", "LedDisplay_PC"),
                                dailyObj("Led Display (Rain)", "LedDisplay_Rain"),
                                dailyObj("Light Panel (Hexagon)", "LightPanel_Hexagon"),
                                dailyObj("Light Panel (Triangle)", "LightPanel_Triangle"),
                                dailyObj("Mechanical Keyboard", "MechanicalKeyboard"),
                                dailyObj("Mechanical Keyboard (Red)", "MechanicalKeyboard_Red"),
                                dailyObj("Mechanical Keyboard (White)", "MechanicalKeyboard_White"),
                                dailyObj("Mouse", "Mouse"),
                                dailyObj("Coffee", "Mug"),
                                dailyObj("Chair (Office Chair)", "OfficeChair"),
                                dailyObj("Paper", "Paper"),
                                dailyObj("Picture Frame (Wall)", "PictureFrame2"),
                                dailyObj("Picture Frame", "PictureFrame"),
                                dailyObj("Picture Frame (Standing)", "PictureFrame_Stand"),
                                dailyObj("Printer Ink (C)", "PrinterInk_C"),
                                dailyObj("Printer Ink (K)", "PrinterInk_K"),
                                dailyObj("Printer Ink (M)", "PrinterInk_M"),
                                dailyObj("Printer Ink (Y)", "PrinterInk_Y"),
                                dailyObj("Used SSD", "SSD 128GB"),
                                dailyObj("Wooden Table", "Table"),
                                dailyObj("Modern Table", "Table_1"),
                                dailyObj("Chair (Toilet)", "ToiletBowl"),
                                dailyObj("VR Glasses", "VR_Glasses"),
                                dailyObj("Wall Mount", "WallMount"),
                                dailyObj("Wall Shelf (Wall)", "WallShelf"),
                                dailyObj("Wall Shelf (RGB, Square)", "WallShelf_1"),
                                dailyObj("Camera", "Webcam"),
                                dailyObj("Insert all", "")
                            )
                            val arr : Array<String>
                            val arrayList = ArrayList<String>()
                            for (i in daily) {
                                arrayList.add(i.name)
                            }
                            arr = arrayList.toTypedArray()
                            // Items referred internally in the code
                            dialog("Daily Market", null, null, null, arr) {_, i ->
                                if (daily[i].name != "Insert all") {
                                    if (daily[i].internalName == "SSD 128GB") {
                                        val random = (0..2147483647).random()
                                        itemArray.put(JSONObject("{\"spawnId\":\"SSD 128GB\",\"id\":$random,\"pos\":{\"x\":22.4925842,\"y\":66.38136,\"z\":3.829202},\"rot\":{\"x\":0.6850593,\"y\":0.1318201,\"z\":-0.712849855,\"w\":0.07184942},\"data\":{\"storageName\":\"Local Disk\",\"password\":\"\",\"files\":[{\"path\":\"System/boot.bin\",\"content\":\"pcos\",\"hidden\":true,\"size\":60000,\"StorageSize\":60000},{\"path\":\"App Downloader.exe\",\"content\":\"\",\"hidden\":false,\"size\":432,\"StorageSize\":432},{\"path\":\"Text Editor.exe\",\"content\":\"\",\"hidden\":false,\"size\":264,\"StorageSize\":264},{\"path\":\"Launcher.exe\",\"content\":\"\",\"hidden\":false,\"size\":94,\"StorageSize\":94}],\"uptime\":2241.17017,\"health\":100.0,\"damaged\":false,\"glue\":false}}"))
                                    } else {
                                        obj = ObjectJson(daily[i].internalName, (0..2147483647).random(), getPos(), Rotation(0.0,0.0,0.0,0.0))
                                        itemArray.put(obj.toJson())
                                    }
                                } else {
                                    for (i in daily) {
                                        if (i.internalName == "SSD 128GB") {
                                            val random = (0..2147483647).random()
                                            itemArray.put(JSONObject("{\"spawnId\":\"SSD 128GB\",\"id\":$random,\"pos\":{\"x\":22.4925842,\"y\":66.38136,\"z\":3.829202},\"rot\":{\"x\":0.6850593,\"y\":0.1318201,\"z\":-0.712849855,\"w\":0.07184942},\"data\":{\"storageName\":\"Local Disk\",\"password\":\"\",\"files\":[{\"path\":\"System/boot.bin\",\"content\":\"pcos\",\"hidden\":true,\"size\":60000,\"StorageSize\":60000},{\"path\":\"App Downloader.exe\",\"content\":\"\",\"hidden\":false,\"size\":432,\"StorageSize\":432},{\"path\":\"Text Editor.exe\",\"content\":\"\",\"hidden\":false,\"size\":264,\"StorageSize\":264},{\"path\":\"Launcher.exe\",\"content\":\"\",\"hidden\":false,\"size\":94,\"StorageSize\":94}],\"uptime\":2241.17017,\"health\":100.0,\"damaged\":false,\"glue\":false}}"))
                                        } else {
                                            obj = ObjectJson(i.internalName, (0..2147483647).random(), getPos(), Rotation(0.0,0.0,0.0,0.0))
                                            itemArray.put(obj.toJson())
                                        }
                                        input.setText(lines[0] + "\n" + jsonObject.toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }
            R.id.saveoptions -> {
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
                val lines = text.lines()
                val init = lines[0].isNotEmpty()
                if (init) {
                    jsonObject = JSONObject(lines[0])
                }
                dialog("Save Options", null, null, null, optionList) {_, i ->
                    fun doItEdittext(title: String, message: String, propertyName: String, int: Boolean, long: Boolean) {
                        val edittext = EditText(this)
                        if (int or long) {
                            edittext.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                        }
                        dialog(title, message ,{_, _ ->
                            if (init) {
                                if (int) {
                                    jsonObject.put(propertyName, parseInt(edittext.text.toString()))
                                } else if (long) {
                                    jsonObject.put(propertyName, parseLong(edittext.text.toString()))
                                } else {
                                    jsonObject.put(propertyName, edittext.text)
                                }
                                input.setText(jsonObject.toString() + "\n" + lines[1])
                            }
                        } , {_, _ ->}, edittext)
                    }
                    fun doItSwitch(title: String, message: String, propertyName: String, switchText: String) {
                        if (Build.VERSION.SDK_INT >= 31) {
                            val checkbox = MaterialSwitch(this)
                            checkbox.text = switchText
                            dialog(title, message ,{_, _ ->
                                if (init) {
                                    jsonObject.put(propertyName, checkbox.isChecked)
                                    input.setText(jsonObject.toString() + "\n" + lines[1])
                                }
                            } , {_, _ ->}, checkbox)
                        } else {
                            val checkbox = SwitchCompat(this)
                            checkbox.text = switchText
                            dialog(title, message ,{_, _ ->
                                if (init) {
                                    jsonObject.put(propertyName, checkbox.isChecked)
                                    input.setText(jsonObject.toString() + "\n" + lines[1])
                                }
                            } , {_, _ ->}, checkbox)
                        }
                    }

                    when (i) {
                        0 -> {
                            doItEdittext("AC Temperature", "Set the temperature of the AC. Max 2147483647 and min -2147483648", "temperature",  true, false)
                        }

                        1 -> {
                            doItSwitch("AC Power", "Switch on or off the AC", "ac",  "Power")
                        }
                        2 -> {
                            doItEdittext("Version", "Set the version of the save.", "version",  false, false)
                        }
                        3 -> {
                            doItEdittext("Money", "Set the money of the save. Max 2147483647 and min -2147483647", "coin",  true, false)
                        }

                        4 -> {
                            doItEdittext("Room", "Set the current room.\n0: Medium\n1: Large\n2: Double Storey\n3: Factory.", "room",  true, false)
                        }

                        5 -> {
                            doItSwitch("Gravity", "Switch on or off gravity.", "gravity", "Gravity")
                        }

                        6 -> {
                            doItSwitch("Hardcore", "Switch on or off hardcore mode.", "hardcore", "Hardcore")
                        }

                        7 -> {
                            doItEdittext("Playtime", "Set the current playtime.", "playtime", false, true)
                        }

                        8 -> {
                            doItSwitch("Light", "Switch on or off the lamp.", "light", "Light")
                        }
                        9 -> {
                            doItEdittext("Sign", "Set the signer of the save.", "sign", false, false)
                        }
                        10 -> {
                            doItEdittext("Save name", "Set the name of the save.", "roomName", false, false)
                        }
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
        resolver = contentResolver
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
            data.data?.also { uri ->
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
            data.data?.also {uri ->
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
        afterData.data?.also {uri ->
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
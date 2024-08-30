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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
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
import android.view.KeyEvent
import android.view.KeyboardShortcutGroup
import android.view.KeyboardShortcutInfo
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat.getSystemService
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

        inputStream?.use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
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

    private lateinit var menus : Menu
    private lateinit var globalVars : GlobalVars
    var text = ""
    var saveString = "" // This way, we dont have to change the edittext, which reduces memory.
    private var decrypt_after_opening = true
    private var encrypt_after_saving = true
    private lateinit var version : String
    private lateinit var input : EditText
    lateinit var save : Button
    private lateinit var ChangelogText : String
    private lateinit var resolver : ContentResolver
    data class Apps(val name : String, val path : String)
    private var fileList = arrayOf(
        Apps("App Manager", "App Downloader.exe"),
        Apps("Daily Market","Daily Market.exe"),
        Apps("Personalization","Personalization.exe"),
        Apps("EZ Mining", "EZ Mining.exe"),
        Apps("Benchmark", "Benchmark.exe"),
        Apps("File Manager", "File Manager.exe"),
        Apps("Disk Management","Disk Management.exe"),
        Apps("System Info","System Info.exe"),
        Apps("Frequency Settings","Frequency Settings.exe"),
        Apps("Frequency Settings","Frequency Settings.exe"),
        Apps("RGB Controller","RGB Controller.exe"),
        Apps("Terminal","Terminal.exe"),
        Apps("Text Editor","Text Editor.exe"),
        Apps("Video Player","Video Player.exe"),
        Apps("Animator","Animator.exe"),
        Apps("My Devices","My Devices.exe"),
        Apps("Browser","Browser.exe"),
        Apps("Boot File", "System/boot.bin"),
        Apps("Virus", "Launcher.exe")
    )
    private fun readTextFromUri(uri: Uri): String {
        val uriThread = ReadTextFromUriThread()
        uriThread.resolver = contentResolver
        uriThread.uri = uri
        val actualThread = Thread(uriThread)
        actualThread.start()
        actualThread.join()
        return uriThread.output
    }
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

    private val writeorread = WriteOrReadThread()
    private val afterread = AfterReadThread()

    private val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) {data ->
        if (data != null) {
            val writeOrReadThread = Thread(writeorread)
            writeorread.clazz = this
            writeorread.resolver = contentResolver
            writeorread.data = data
            writeorread.input = input
            writeorread.decrypt_after_opening = decrypt_after_opening
            writeorread.encrypt_after_saving = encrypt_after_saving
            writeorread.WriteOrRead = false
            writeOrReadThread.start()
            writeOrReadThread.join()
            System.gc()
        }
    }
    val saveTheFile = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {uri ->
        if (uri != null) {
            val afterReadThread = Thread(afterread)
            afterread.resolver = contentResolver
            afterread.afterData = uri
            afterread.text = saveString
            afterReadThread.start()
            System.gc()
        }
    }

    private val openandSavetotxt = registerForActivityResult(ActivityResultContracts.OpenDocument()) {uri ->
        if (uri != null) {
            val writeOrReadThread = Thread(writeorread)
            writeorread.clazz = this
            writeorread.resolver = contentResolver
            writeorread.data = uri
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

    fun dialog(title: String, message: String, okListener: OnClickListener?, cancelListener: OnClickListener?) {
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                .setMessage(message)
                .setTitle(title)
            if (okListener != null) { builder.setPositiveButton("Ok", okListener) }
            if (cancelListener != null) { builder.setNegativeButton("Cancel", cancelListener) }
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                .setMessage(message)
                .setTitle(title)
            if (okListener != null) { builder.setPositiveButton("Ok", okListener) }
            if (cancelListener != null) { builder.setNegativeButton("Cancel", cancelListener) }

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
                .setTitle(title)
                .setItems(adapter, adapterOnClickListener)
            if (message != null) { builder.setMessage(message) }
            if (okListener != null) { builder.setPositiveButton("Ok", okListener) }
            if (cancelListener != null) { builder.setNegativeButton("Cancel", cancelListener) }
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
            if (okListener != null) { builder.setPositiveButton("Ok", okListener) }
            if (cancelListener != null) { builder.setNegativeButton("Cancel", cancelListener) }
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
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                .setTitle(title)
                .setMultiChoiceItems(adapter, boolArray, adapterOnClickListener)
            if (message != null) { builder.setMessage(message) }
            if (okListener != null) { builder.setPositiveButton("Ok", okListener) }
            if (cancelListener != null) { builder.setNegativeButton("Cancel", cancelListener) }
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
            if (message != null) { builder.setMessage(message) }
            builder
                .setTitle(title)
                .setMultiChoiceItems(adapter, boolArray, adapterOnClickListener)
            if (okListener != null) { builder.setPositiveButton("Ok", okListener) }
            if (cancelListener != null) { builder.setNegativeButton("Cancel", cancelListener) }
            builder.show()
        }
    }

    private fun dialog(title: String,
                       message: String?,
                       okListener: OnClickListener?,
                       cancelListener: OnClickListener?,
                       view : View
    ) {
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
                .setTitle(title)
                .setView(view)
            if (message != null) { builder.setMessage(message) }
            if (okListener != null) { builder.setPositiveButton("Ok", okListener) }
            if (cancelListener != null) { builder.setNegativeButton("Cancel", cancelListener) }
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setIcon(R.drawable.baseline_info_outline_24)
            if (message != null) { builder.setMessage(message) }
            builder
                .setTitle(title)
                .setView(view)
            if (okListener != null) { builder.setPositiveButton("Ok", okListener) }
            if (cancelListener != null) { builder.setNegativeButton("Cancel", cancelListener) }
            builder.show()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (event?.isCtrlPressed == true) {
                    findViewById<Button>(R.id.decryptencrypt).performClick()
                }
            }
            KeyEvent.KEYCODE_F1 -> {
                startActivity(Intent(applicationContext, HelpActivity::class.java))
            }
            KeyEvent.KEYCODE_INSERT -> {
                menus.performIdentifierAction(R.id.insert, 0)
            }
            KeyEvent.KEYCODE_F2 -> {
                menus.performIdentifierAction(R.id.saveoptions, 0)
            }
            KeyEvent.KEYCODE_F3 -> {
                menus.performIdentifierAction(R.id.dump, 0)
            }
            KeyEvent.KEYCODE_F4 -> {
                menus.performIdentifierAction(R.id.clear, 0)
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onProvideKeyboardShortcuts(
        data: MutableList<KeyboardShortcutGroup>?,
        menu: Menu?,
        deviceId: Int
    ) {
        super.onProvideKeyboardShortcuts(data, menu, deviceId)
        val appShortcutGroup = KeyboardShortcutGroup(
            "Save Editor",
            listOf(
                KeyboardShortcutInfo("Decrypt/Encrypt", KeyEvent.KEYCODE_ENTER, KeyEvent.META_CTRL_ON),
                KeyboardShortcutInfo("Help", KeyEvent.KEYCODE_F1, 0),
                KeyboardShortcutInfo("Insert Object", KeyEvent.KEYCODE_INSERT, 0),
                KeyboardShortcutInfo("Save Options", KeyEvent.KEYCODE_F2, 0),
                KeyboardShortcutInfo("Password Dumper", KeyEvent.KEYCODE_F3, 0),
                KeyboardShortcutInfo("Cleanup", KeyEvent.KEYCODE_F4, 0)
            )
        )
        val helpShortcutGroup = KeyboardShortcutGroup(
            "Help",
            listOf(
                KeyboardShortcutInfo("Back", KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.META_ALT_ON),
                KeyboardShortcutInfo("Forward", KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.META_ALT_ON)
            )
        )
        data?.add(appShortcutGroup)
        data?.add(helpShortcutGroup)
    }

    @SuppressLint("SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shortcuts -> {
                if (Build.VERSION.SDK_INT >= 24) {
                    requestShowKeyboardShortcuts()
                }
            }
            R.id.decrypt_after_opening -> {
                decrypt_after_opening = decrypt_after_opening.not()
            }
            R.id.encrypt_after_saving -> {
                encrypt_after_saving = encrypt_after_saving.not()
            }
            R.id.changelog -> {
                dialog("Changelog (version $version)", globalVars.ChangelogText, {_,_->}, null)
            }
            R.id.about -> {
                dialog("PC Simulator Save Editor Android Port (version $version)", """
                        |Created by Mokka Chocolata.
                        |Free, and open source.
                        |Get beta builds at the Actions tab at the GitHub repository.
                        |Report any issues at the Issues tab at the GitHub repository.
                        |Please attack the PC Simulator discord by sharing invites to the PC Simulator Save Editor Discord!
                        |This app is licensed with GPLv3.0.
                        |""".trimMargin(), {_,_->}, null
                )
            }
            R.id.help -> {
                System.gc()
                startActivity(Intent(applicationContext, HelpActivity::class.java))
            }
            R.id.discord -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/GXRECJjhVr")))
            }
            R.id.clear -> {
                // Clear all cardboard boxes
                val text = input.text.toString()
                val lines = text.lines()
                val jsonObject = JSONObject(lines[1])
                val itemArray = jsonObject.getJSONArray("itemData")
                for (i in 0 until itemArray.length()) {
                    if (i < itemArray.length()) {
                        if (itemArray.getJSONObject(i).getString("spawnId") == "CardboardBox" || itemArray.getJSONObject(i).getString("spawnId") == "LongCardboardBox" || itemArray.getJSONObject(i).getString("spawnId") == "CardboardBox 2") {
                            itemArray.remove(i)
                        }
                    }
                }
                for (i in 0 until itemArray.length()) {
                    if (i < itemArray.length()) {
                        if (itemArray.getJSONObject(i).getString("spawnId").contains("Part_") || itemArray.getJSONObject(i).getString("spawnId") == "Part") {
                            itemArray.remove(i)
                        }
                    }
                }
                input.setText(lines[0] + "\n" + jsonObject.toString())
            }
            R.id.dump -> {
                val text = input.text.toString()
                val jsonObject = JSONObject(text.lines()[1])
                val itemArray = jsonObject.getJSONArray("itemData")
                var pwd = ""
                for (i in 0 until itemArray.length()) {
                    val storage = itemArray.getJSONObject(i)
                    val spawnId = storage.getString("spawnId")
                    if (spawnId.contains("SSD") or spawnId.contains("HDD") or spawnId.contains("SSD_M.2") or spawnId.contains("FlashDrive")) {
                        val data = storage.getJSONObject("data")
                        if (data.has("storageData")) {
                            val storageData = data.getJSONObject("storageData")
                            val password = storageData.getString("userPassword")
                            if (password.isNotEmpty()) {
                                pwd += "$password : $spawnId \n"
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
                    "Custom Spawn ID",
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
                    "CPU",
                    "RAM",
                    "Cooler"
                )
                dialog(resources.getString(R.string.insert), null, null, null, itemList) { _, i ->
                    val text = input.text.toString()
                    val jsonObject = JSONObject(text.lines()[1])
                    val lines = text.lines()
                    val itemArray = jsonObject.getJSONArray("itemData")
                    val position = Position(
                        (jsonObject.get("playerData") as JSONObject).getDouble("x"),
                        (jsonObject.get("playerData") as JSONObject).getDouble("y"),
                        (jsonObject.get("playerData") as JSONObject).getDouble("z")
                    )
                    fun doit() {
                        val obj = ObjectJson(itemList[i], (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0))
                        itemArray.put(obj.toJson())
                        input.setText(lines[0] + "\n" + jsonObject.toString())
                    }
                    fun doitBanner() {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    fun putFile(i: Int, array: JSONArray) {
                        when(i) {
                            17 -> {
                                val file = FileObjectJson(fileList[i].path, "pcos", true, 0, 0)
                                array.put(file.toJson())
                            }
                            else -> {
                                val file = FileObjectJson(fileList[i].path, "", false, 0, 0)
                                array.put(file.toJson())
                            }
                        }
                    }

                    fun doitMarket(prefix: String?, array: Array<String>, dialogName: String) {
                        var obj: ObjectJson

                        dialog(dialogName, null, null, null, array) { _, i ->
                            obj = ObjectJson(if(prefix != null) prefix + array[i] else "" + array[i], (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0))
                            itemArray.put(obj.toJson())
                            input.setText(lines[0] + "\n" + jsonObject.toString())
                        }
                    }
                    val arr : Array<String>
                    val arrayList = ArrayList<String>()
                    for (files in fileList) {
                        arrayList.add(files.name)
                    }
                    arr = arrayList.toTypedArray()
                    when (i) {
                        0 -> {
                            val edittext = EditText(this)
                            var obj: ObjectJson
                            dialog("Custom Spawn ID", "Add an object with a custom spawn ID.", {_,_->
                                obj = ObjectJson(edittext.text.toString(), (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0))
                                itemArray.put(obj.toJson())
                                input.setText(lines[0] + "\n" + jsonObject.toString())
                            }, {_,_->}, edittext)
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
                            doit()
                        }
                        5 -> {
                            doitBanner()
                        }

                        6 -> {
                            var driveName : String
                            val boolArray = BooleanArray(fileList.size)

                            var password : String
                            val edittext = EditText(this)
                            dialog("USB Drive Name", "Set the storage name that appears in Disk Management and when you hold it.", { _, _ ->
                                driveName = edittext.text.toString()
                                val edittextUSB = EditText(this)
                                edittextUSB.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                dialog("Password", "Set the password of this drive.", { _, _ ->
                                    password = edittextUSB.text.toString()
                                    // Null the message out because the items wont appear
                                    dialogMultiChoice("Files", null, {_, _ ->
                                        val array = JSONArray()
                                        for (checked in boolArray.indices) {
                                            if (boolArray[checked]) {
                                                putFile(checked, array)
                                            }
                                        }
                                        val drive = USBObjectJson((0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array)
                                        itemArray.put(drive.toJson())
                                        input.setText(lines[0] + "\n" + jsonObject.toString())
                                    }, null, arr, {_, which, isChecked ->
                                        boolArray[which] = isChecked
                                    }, boolArray)
                                }, {_,_->}, edittextUSB)
                            }, {_,_->}, edittext)
                        }

                        7 -> {
                            var driveName: String

                            val boolArray = BooleanArray(fileList.size)

                            var password: String
                            var size : String
                            val edittext = EditText(this)
                            dialog("SSD Drive Name", "Set the storage name that appears in Disk Management and when you hold it.", { _, _ ->
                                driveName = edittext.text.toString()
                                val edittextssd = EditText(this)
                                edittextssd.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                dialog("Password", "Set the password of this drive.", { _, _ ->
                                    password = edittextssd.text.toString()
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
                                            for (checked in boolArray.indices) {
                                                if (boolArray[checked]) {
                                                    putFile(checked, array)
                                                }
                                            }
                                            val drive = SSDObjectJson(size, (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array, "User")
                                            itemArray.put(drive.toJson())
                                            input.setText(lines[0] + "\n" + jsonObject.toString())
                                        }, null, arr, {_, which, isChecked ->
                                            boolArray[which] = isChecked
                                        }, boolArray)
                                    }
                                }, {_,_->}, edittextssd)
                            }, {_,_->}, edittext)}

                        8 -> {
                            var driveName: String
                            val boolArray = BooleanArray(fileList.size)

                            var password: String
                            var size : String
                            val edittext = EditText(this)
                            dialog("M.2 NVMe SSD Drive Name", "Set the storage name that appears in Disk Management and when you hold it.", { _, _ ->
                                driveName = edittext.text.toString()
                                val edittextm2 = EditText(this)
                                edittextm2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                dialog("Password", "Set the password of this drive.", { _, _ ->
                                    password = edittextm2.text.toString()
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
                                            for (checked in boolArray.indices) {
                                                if (boolArray[checked]) {
                                                    putFile(checked, array)
                                                }
                                            }
                                            val drive = M2ObjectJson(size, (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array, "User")
                                            itemArray.put(drive.toJson())
                                            input.setText(lines[0] + "\n" + jsonObject.toString())
                                        }, null, arr, {_, which, isChecked ->
                                            boolArray[which] = isChecked
                                        }, boolArray)
                                    }
                                }, {_,_->}, edittextm2)
                            }, {_,_->}, edittext)}

                        9 -> {
                            var driveName: String
                            val boolArray = BooleanArray(fileList.size)

                            var password: String
                            var size: String
                            val edittext = EditText(this)
                            dialog("HDD Drive Name", "Set the storage name that appears in Disk Management and when you hold it.", { _, _ ->
                                driveName = edittext.text.toString()
                                val edittextHDD = EditText(this)
                                edittextHDD.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                dialog("Password", "Set the password of this drive.", { _, _ ->
                                    password = edittextHDD.text.toString()
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
                                            for (checked in boolArray.indices) {
                                                if (boolArray[checked]) {
                                                    putFile(checked, array)
                                                }
                                            }
                                            val drive = HDDObjectJson(size, (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array, "User")
                                            itemArray.put(drive.toJson())
                                            input.setText(lines[0] + "\n" + jsonObject.toString())
                                        }, null, arr, {_, which, isChecked ->
                                            boolArray[which] = isChecked
                                        }, boolArray)
                                    }
                                }, {_,_->}, edittextHDD)
                            }, {_,_->}, edittext)}

                        10 -> {
                            var obj: ObjectJson
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
                            val arrDaily : Array<String>
                            val dailyArrList = ArrayList<String>()
                            for (davey in daily) {
                                dailyArrList.add(davey.name)
                            }
                            arrDaily = dailyArrList.toTypedArray()
                            // Items referred internally in the code
                            dialog("Daily Market", null, null, null, arrDaily) { _, index ->
                                if (daily[index].name != "Insert all") {
                                    if (daily[index].internalName == "SSD 128GB") {
                                        val random = (0..2147483647).random()
                                        itemArray.put(JSONObject("{\"spawnId\":\"SSD 128GB\",\"id\":$random,\"pos\":{\"x\":22.4925842,\"y\":66.38136,\"z\":3.829202},\"rot\":{\"x\":0.6850593,\"y\":0.1318201,\"z\":-0.712849855,\"w\":0.07184942},\"data\":{\"storageName\":\"Local Disk\",\"password\":\"\",\"files\":[{\"path\":\"System/boot.bin\",\"content\":\"pcos\",\"hidden\":true,\"size\":60000,\"StorageSize\":60000},{\"path\":\"App Downloader.exe\",\"content\":\"\",\"hidden\":false,\"size\":432,\"StorageSize\":432},{\"path\":\"Text Editor.exe\",\"content\":\"\",\"hidden\":false,\"size\":264,\"StorageSize\":264},{\"path\":\"Launcher.exe\",\"content\":\"\",\"hidden\":false,\"size\":94,\"StorageSize\":94}],\"uptime\":2241.17017,\"health\":100.0,\"damaged\":false,\"glue\":false}}"))
                                    } else {
                                        obj = ObjectJson(daily[index].internalName, (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0))
                                        itemArray.put(obj.toJson())
                                    }
                                } else {
                                    for (daveys in daily) {
                                        if (daveys.internalName == "SSD 128GB") {
                                            val random = (0..2147483647).random()
                                            itemArray.put(JSONObject("{\"spawnId\":\"SSD 128GB\",\"id\":$random,\"pos\":{\"x\":22.4925842,\"y\":66.38136,\"z\":3.829202},\"rot\":{\"x\":0.6850593,\"y\":0.1318201,\"z\":-0.712849855,\"w\":0.07184942},\"data\":{\"storageName\":\"Local Disk\",\"password\":\"\",\"files\":[{\"path\":\"System/boot.bin\",\"content\":\"pcos\",\"hidden\":true,\"size\":60000,\"StorageSize\":60000},{\"path\":\"App Downloader.exe\",\"content\":\"\",\"hidden\":false,\"size\":432,\"StorageSize\":432},{\"path\":\"Text Editor.exe\",\"content\":\"\",\"hidden\":false,\"size\":264,\"StorageSize\":264},{\"path\":\"Launcher.exe\",\"content\":\"\",\"hidden\":false,\"size\":94,\"StorageSize\":94}],\"uptime\":2241.17017,\"health\":100.0,\"damaged\":false,\"glue\":false}}"))
                                        } else {
                                            obj = ObjectJson(daveys.internalName, (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0))
                                            itemArray.put(obj.toJson())
                                        }
                                        input.setText(lines[0] + "\n" + jsonObject.toString())
                                    }
                                }
                            }
                        }

                        11 -> {
                            doitMarket("CPU ", arrayOf("RMD Ryzen 9 7950X",
                                "i9-12900K",
                                "i9-9900K",
                                "i9-7900X",
                                "i7-14700K",
                                "i7-13700K",
                                "i7-8700K",
                                "i5-8400",
                                "i3-8300",
                                "Celeron G3920"), "CPU")
                        }

                        12 -> {
                            doitMarket("RAM ", arrayOf("32GB",
                                "32GB(RGB)",
                                "16GB",
                                "16GB(RGB)",
                                "8GB",
                                "8GB(RGB)",
                                "4GB",
                                "4GB(RGB)",
                                "2GB",
                                "1GB"), "RAM")
                        }

                        13 -> {
                            doitMarket(null, arrayOf("Cooler(RGB)",
                                "Cooler",
                                "TowerCooler(RGB)",
                                "TowerCooler",
                                "WaterCooler",
                                "CaseFan(RGB)",
                                "CaseFan",
                                "WaterCooler_1"), "Cooler")
                        }
                    }
                }
            }
            R.id.license -> {
                dialog("Open Source Licenses", """
                    This app is licensed with GPLv3.0.
                    AndroidX (Apache License 2.0)
                    Android (Apache License 2.0)
                    JSONJava (Public Domain)
                """.trimIndent(), {_,_->}, null)
            }
            R.id.saveoptions -> {
                val optionList = arrayOf("AC Temperature",
                    "AC Power",
                    "Version",
                    "Money",
                    "Room",
                    "Gravity",
                    "Hardcore",
                    "Playtime",
                    "Light",
                    "Sign",
                    "Save name")
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

        if (menu != null) {
            menus = menu
        }
        if (Build.VERSION.SDK_INT <= 24) {
            menu?.findItem(R.id.shortcuts)?.isEnabled = false
        }

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
            if (input.text.isNotEmpty()) {
                input.setText(functions.Decrypt(input.text.toString()))
            }
        }

        val open = findViewById<Button>(R.id.open)
        save = findViewById(R.id.save)
        val copy = findViewById<Button>(R.id.clipboard)

        open.setOnClickListener {_ ->
            System.gc()
            pickFile.launch(arrayOf("application/octet-stream"))
        }

        save.setOnClickListener {_ ->
            System.gc()
            if (input.text.isNotEmpty()) {
                saveString =
                    if (encrypt_after_saving) functions.Decrypt(input.text.toString()) else input.text.toString()
                saveTheFile.launch("Save.pc")
            }
        }

        val decryptToTxt = findViewById<Button>(R.id.decryptToTxt)

        decryptToTxt.setOnClickListener { _ ->
            System.gc()
            openandSavetotxt.launch(arrayOf("application/octet-stream"))
        }

        copy.setOnClickListener{_ ->
            System.gc()
            if (input.text.isNotEmpty()) {
                val clip = ClipData.newPlainText("PC Simulator Save", input.text.toString())
                getSystemService(
                    this,
                    ClipboardManager::class.java
                )?.setPrimaryClip(clip)
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
                if (decrypt_after_opening) {
                    input.setText(functions.Decrypt(readTextFromUri(appLinkData)))
                } else {
                    input.setText(readTextFromUri(appLinkData))
                }
            }
        }

    }
}


class WriteOrReadThread : Runnable{
    var WriteOrRead by Delegates.notNull<Boolean>()
    lateinit var input : EditText
    lateinit var data : Uri
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
            val thread = Thread(functions)
            functions.input = readTextFromUri(data)
            thread.start()
            thread.join()
            if (decrypt_after_opening) {
                input.setText(functions.Output)
            } else {
                input.setText(readTextFromUri(data))
            }
        } else if (saveToTxt) {
            val thread = Thread(functions)
            functions.input = readTextFromUri(data)
            thread.start()
            thread.join()
            Log.d("App", functions.Output)
            clazz.saveString = functions.Output
            clazz.saveTheFile.launch("Save.txt")
        } }
    }

class AfterReadThread : Runnable{
    lateinit var afterData : Uri
    lateinit var resolver : ContentResolver
    lateinit var text: String

    override fun run() {
        try {
                resolver.openFileDescriptor(afterData, "w")?.use { it ->
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
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
import android.content.DialogInterface
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
import androidx.core.net.toUri
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
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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
    var saveString = "" // This way, we don't have to change the edittext, which reduces memory.
    private var decrypt_after_opening = true
    private var encrypt_after_saving = true
    private lateinit var version : String
    private lateinit var input : EditText
    lateinit var save : Button
    private lateinit var ChangelogText : String
    private lateinit var resolver : ContentResolver
    data class Mod(
        val name: String,
        val description: String?,
        val license: String?,
        val creator: String?,
        val repositoryUrl: String?,
        val version: String?,
        val source: String
    )
    var lua_openFile_dat : Uri? = null
    var lua_openFile_picked = false
    // lateinit var lua_global : Globals

    val result = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            lua_openFile_dat = it
            lua_openFile_picked = true
        } else {
            lua_openFile_picked = false
        }
    }
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
    private class PCSimulatorSaveEditorUtilClass : TwoArgFunction() {
        lateinit var globalVars : GlobalVars
        lateinit var activity2: MainActivity2
        lateinit var edittext: EditText
        lateinit var menu: Menu
        override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {
            val library = tableOf()
            library["Version"] = LuaValue.valueOf(globalVars.version)
            library["Platform"] = valueOf(0)
            library["Print"] = object : OneArgFunction() {
                override fun call(arg: LuaValue?): LuaValue {
                    arg?.toString()?.let { Log.i("Script", it) }
                    return NONE
                }
            }
            library["DisplayDialog"] = object : TwoArgFunction() {
                override fun call(arg1: LuaValue?, arg2: LuaValue?): LuaValue {
                    if (Build.VERSION.SDK_INT >= 31) {
                        val builder = MaterialAlertDialogBuilder(activity2)
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        builder.setMessage(arg2?.toString())
                        builder.setPositiveButton("Ok") {_,_->}
                        builder.show()
                    } else {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(activity2)
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        builder.setMessage(arg2?.toString())
                        builder.setPositiveButton("Ok") {_,_->}
                        builder.show()
                    }
                    return NONE
                }
            }
            library["DecryptString"] = object : OneArgFunction() {
                override fun call(arg1: LuaValue?): LuaValue =
                    LuaValue.valueOf(MainFunctions().Decrypt(arg1?.toString()))
            }
            library["SetSaveContents"] = object : OneArgFunction() {
                override fun call(contents: LuaValue?): LuaValue {
                    edittext.setText(contents?.toString())
                    return NONE
                }
            }
            library["GetSaveContents"] = object : ZeroArgFunction() {
                override fun call(): LuaValue =
                    LuaValue.valueOf(edittext.text.toString())
            }
            library["AddMenuItem"] = object : TwoArgFunction() {
                override fun call(name: LuaValue?, callback: LuaValue?): LuaValue {
                    val item = menu.add(name?.toString())
                    item.setOnMenuItemClickListener {
                        if (callback?.isfunction() == true) callback.call()
                        false
                    }
                    val delFunction = tableOf()
                    delFunction.set("Delete", object : ZeroArgFunction() {
                        override fun call(): LuaValue {
                            menu.removeItem(item.itemId)
                            return NONE
                        }
                    })
                    return delFunction
                }
            }
            library["OpenFile"] = object : OneArgFunction() {
                override fun call(memeType : LuaValue?): LuaValue {
                    activity2.lua_openFile_dat = null
                    activity2.lua_openFile_picked = false
                    if (memeType != null) activity2.result.launch(arrayOf(memeType.toString()))
                    else activity2.result.launch(arrayOf("*/*"))

                    val table = tableOf()
                    if (activity2.lua_openFile_dat != null)
                        table.set("Text", LuaValue.valueOf(activity2.readTextFromUri(activity2.lua_openFile_dat!!)))
                    else
                        table.set("Text", "")
                    table.set("Picked", LuaValue.valueOf(activity2.lua_openFile_picked))
                    return table
                }
            }
            library["DisplayEditTextDialog"] = object : ThreeArgFunction() {
                override fun call(arg1: LuaValue?, message: LuaValue?, callback: LuaValue?): LuaValue {
                    val edittext = EditText(activity2)
                    if (Build.VERSION.SDK_INT >= 31) {
                        val builder = MaterialAlertDialogBuilder(activity2)
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        builder.setView(edittext)
                        builder.setMessage(message?.toString())
                        builder.setPositiveButton("Ok") { _,_->
                            callback?.call(LuaValue.valueOf(0), LuaValue.valueOf(edittext.text.toString()))
                        }
                        builder.setNegativeButton("Cancel") { _,_->
                            callback?.call(LuaValue.valueOf(1), LuaValue.valueOf(""))
                        }
                        builder.show()
                    } else {
                        val builder = AlertDialog.Builder(activity2)
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        builder.setView(edittext)
                        builder.setMessage(message?.toString())
                        builder.setPositiveButton("Ok") { _,_->
                            callback?.call(LuaValue.valueOf(0), LuaValue.valueOf(edittext.text.toString()))
                        }
                        builder.setNegativeButton("Cancel") { _,_->
                            callback?.call(LuaValue.valueOf(1), LuaValue.valueOf(""))
                        }
                        builder.show()
                    }
                    return NONE
                }
            }
            library["DisplayCheckboxDialog"] = object : ThreeArgFunction() {
                override fun call(arg1: LuaValue?, message: LuaValue?, callback: LuaValue?): LuaValue {
                    if (Build.VERSION.SDK_INT >= 31) {
                        val edittext = MaterialSwitch(activity2)
                        edittext.text = message?.toString()
                        val builder = MaterialAlertDialogBuilder(activity2)
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        builder.setView(edittext)
                        builder.setPositiveButton("Ok") { _,_->
                            callback?.call(LuaValue.valueOf(0), LuaValue.valueOf(edittext.isChecked))
                        }
                        builder.setNegativeButton("Cancel") { _,_->
                            callback?.call(LuaValue.valueOf(1), LuaValue.valueOf(edittext.isChecked))
                        }
                        builder.show()
                    } else {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(activity2)
                        val edittext = MaterialSwitch(activity2)
                        edittext.text = message?.toString()
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        builder.setView(edittext)
                        builder.setPositiveButton("Ok") { _,_->
                            callback?.call(LuaValue.valueOf(0), LuaValue.valueOf(edittext.isChecked))
                        }
                        builder.setNegativeButton("Cancel") { _,_->
                            callback?.call(LuaValue.valueOf(1), LuaValue.valueOf(edittext.isChecked))
                        }
                        builder.show()
                    }
                    return NONE
                }
            }
            library["DisplayListDialog"] = object : ThreeArgFunction() {
                override fun call(arg1: LuaValue?, list: LuaValue?, callback: LuaValue?): LuaValue {
                    if (Build.VERSION.SDK_INT >= 31) {
                        val builder = MaterialAlertDialogBuilder(activity2)
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        val arr = arrayListOf<String>()
                        if (list?.istable() == true) {
                            for (i in 0..list.length())
                                if (list[i].isstring()) arr.add(list[i].toString())
                            builder.setItems(arr.toTypedArray()) {_, i ->
                                callback?.call(LuaValue.valueOf(i))
                            }
                        }
                        builder.show()
                    } else {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(activity2)
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        val arr = arrayListOf<String>()
                        if (list?.istable() == true) {
                            for (i in 0..list.length()) {
                                if (list[i].isstring()) arr.add(list[i].toString())
                            }
                            builder.setItems(arr.toTypedArray()) {_, i ->
                                callback?.call(LuaValue.valueOf(i))
                            }
                        }
                        builder.show()
                    }
                    return NONE
                }

            }
            library["DisplayMultiChoiceListDialog"] = object : ThreeArgFunction() {
                override fun call(arg1: LuaValue?, list: LuaValue?, callback: LuaValue?): LuaValue {
                    if (Build.VERSION.SDK_INT >= 31) {
                        val builder = MaterialAlertDialogBuilder(activity2)
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        val arr = arrayListOf<String>()
                        if (list?.istable() == true) {
                            for (i in 0..list.length()) {
                                if (list[i].isstring()) arr.add(list[i].toString())
                            }
                            val checkedItems = BooleanArray(arr.size)
                            builder.setMultiChoiceItems(arr.toTypedArray(), checkedItems) { _, which, isChecked ->
                                checkedItems[which] = isChecked
                            }
                            builder.setPositiveButton("Ok") { _,_->
                                val items = arrayListOf<LuaValue>()
                                for (i in checkedItems.indices) {
                                    items.add(LuaValue.valueOf(checkedItems[i]))
                                }
                                callback?.call(LuaValue.valueOf(0), tableOf(null, items.toTypedArray()))
                            }
                            builder.setNegativeButton("Cancel") { _,_->
                                callback?.call(LuaValue.valueOf(1), LuaValue.NIL)
                            }
                        }
                        builder.show()
                    } else {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(activity2)
                        builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(arg1?.toString())
                        val arr = arrayListOf<String>()
                        if (list?.istable() == true) {
                            for (i in 0..list.length())
                                if (list[i].isstring()) arr.add(list[i].toString())
                            val checkedItems = BooleanArray(arr.size)
                            builder.setMultiChoiceItems(arr.toTypedArray(), checkedItems) { _, which, isChecked ->
                                checkedItems[which] = isChecked
                            }
                            builder.setPositiveButton("Ok") { _,_->
                                val items = arrayListOf<LuaValue>()
                                for (i in checkedItems.indices) items.add(LuaValue.valueOf(checkedItems[i]))
                                callback?.call(LuaValue.valueOf(0), tableOf(null, items.toTypedArray()))
                            }
                            builder.setNegativeButton("Cancel") { _,_->
                                callback?.call(LuaValue.valueOf(1), LuaValue.NIL)
                            }
                        }
                        builder.show()
                    }
                    return NONE
                }
            }
            library["OpenURL"] = object : OneArgFunction() {
                override fun call(uri: LuaValue?): LuaValue {
                    activity2.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri?.toString())))
                    return NONE
                }
            }
            library["PLATFORM_ANDROID"] = 0
            library["PLATFORM_JAVA"] = 1
            library["PLATFORM_UNITY"] = 2
            library["DIALOG_BUTTON_OK"] = 0
            library["DIALOG_BUTTON_CANCEL"] = 1
            env?.set("SaveEditor", library)
            return library
        }

    }
    private fun doOnThread(obj: Runnable, wait: Boolean) {
        val actualThread = Thread(obj)
        actualThread.start()
        if (wait) actualThread.join()
    }
    private fun readTextFromUri(uri: Uri): String {
        val uriThread = ReadTextFromUriThread()
        uriThread.resolver = contentResolver
        uriThread.uri = uri
        doOnThread(uriThread, true)
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
            Thread { bm.compress(Bitmap.CompressFormat.JPEG, 100, baos) }.apply {
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
            writeorread.clazz = this
            writeorread.resolver = contentResolver
            writeorread.data = data
            writeorread.input = input
            writeorread.decrypt_after_opening = decrypt_after_opening
            writeorread.encrypt_after_saving = encrypt_after_saving
            writeorread.WriteOrRead = false
            doOnThread(writeorread, true)
            System.gc()
        }
    }
    private val pickMod = registerForActivityResult(ActivityResultContracts.OpenDocument()) {data ->
        if (data != null) {
            Log.d("App", readTextFromUri(data))
            val file = File(filesDir, JSONObject(readTextFromUri(data)).getString("name") + ".pcssemod")
            file.writeText(readTextFromUri(data))
        }
    }
    val saveTheFile = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {uri ->
        if (uri != null) {
            afterread.resolver = contentResolver
            afterread.afterData = uri
            afterread.text = saveString
            doOnThread(afterread, false)
            System.gc()
        }
    }

    private val openandSavetotxt = registerForActivityResult(ActivityResultContracts.OpenDocument()) {uri ->
        if (uri != null) {
            writeorread.clazz = this
            writeorread.resolver = contentResolver
            writeorread.data = uri
            writeorread.input = input
            writeorread.decrypt_after_opening = decrypt_after_opening
            writeorread.encrypt_after_saving = encrypt_after_saving
            writeorread.WriteOrRead = true
            writeorread.saveToTxt = true
            writeorread.doClazz = afterread
            doOnThread(writeorread, true)
            System.gc()
        }
    }

    fun dialog(title: String, message: String, okListener: OnClickListener?, cancelListener: OnClickListener?) {
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setIcon(R.drawable.baseline_info_outline_24).setMessage(message).setTitle(title)
            if (okListener != null) builder.setPositiveButton("Ok", okListener)
            if (cancelListener != null) builder.setNegativeButton("Cancel", cancelListener)
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setIcon(R.drawable.baseline_info_outline_24).setMessage(message).setTitle(title)
            if (okListener != null) builder.setPositiveButton("Ok", okListener)
            if (cancelListener != null) builder.setNegativeButton("Cancel", cancelListener)
            builder.show()
        }
    }

    fun dialog(
        title: String,
        message: String?,
        okListener: OnClickListener?,
        cancelListener: OnClickListener?,
        adapter: Array<String>,
        adapterOnClickListener: (dialog: DialogInterface, which: Int) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(title).setItems(adapter, adapterOnClickListener)
            if (message != null) builder.setMessage(message)
            if (okListener != null) builder.setPositiveButton("Ok", okListener)
            if (cancelListener != null) builder.setNegativeButton("Cancel", cancelListener)
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(title).setItems(adapter, adapterOnClickListener)
            if (message != null) builder.setMessage(message)
            if (okListener != null) builder.setPositiveButton("Ok", okListener)
            if (cancelListener != null) builder.setNegativeButton("Cancel", cancelListener)
            builder.show()
        }
    }
    fun dialog(
        title: String,
        message: String?,
        okListener: OnClickListener?,
        cancelListener: OnClickListener?,
        okText : String?,
        cancelText : String?,
        adapter: Array<String>,
        adapterOnClickListener: (dialog: DialogInterface, which: Int) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= 31) {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(title).setItems(adapter, adapterOnClickListener)
            if (message != null) builder.setMessage(message)
            if (okListener != null) builder.setPositiveButton(okText, okListener)
            if (cancelListener != null) builder.setNegativeButton(cancelText, cancelListener)
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(title).setItems(adapter, adapterOnClickListener)
            if (message != null) builder.setMessage(message)
            if (okListener != null) builder.setPositiveButton(okText, okListener)
            if (cancelListener != null) builder.setNegativeButton(cancelText, cancelListener)
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
            builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(title).setMultiChoiceItems(adapter, boolArray, adapterOnClickListener)
            if (message != null) builder.setMessage(message)
            if (okListener != null) builder.setPositiveButton("Ok", okListener)
            if (cancelListener != null) builder.setNegativeButton("Cancel", cancelListener)
            builder.show()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(title).setMultiChoiceItems(adapter, boolArray, adapterOnClickListener)
            if (message != null) builder.setMessage(message)
            if (okListener != null) builder.setPositiveButton("Ok", okListener)
            if (cancelListener != null) builder.setNegativeButton("Cancel", cancelListener)
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
            builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(title).setView(view)
            if (message != null) builder.setMessage(message)
            if (okListener != null) builder.setPositiveButton("Ok", okListener)
            if (cancelListener != null) builder.setNegativeButton("Cancel", cancelListener)
            builder.show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setIcon(R.drawable.baseline_info_outline_24).setTitle(title).setView(view)
            if (message != null) builder.setMessage(message)
            if (okListener != null) builder.setPositiveButton("Ok", okListener)
            if (cancelListener != null) builder.setNegativeButton("Cancel", cancelListener)
            builder.show()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER ->
                if (event?.isCtrlPressed == true)
                    findViewById<Button>(R.id.decryptencrypt).performClick()
            KeyEvent.KEYCODE_F1 ->
                startActivity(Intent(applicationContext, HelpActivity::class.java))
            KeyEvent.KEYCODE_INSERT ->
                menus.performIdentifierAction(R.id.insert, 0)
            KeyEvent.KEYCODE_F2 ->
                menus.performIdentifierAction(R.id.saveoptions, 0)
            KeyEvent.KEYCODE_F3 ->
                menus.performIdentifierAction(R.id.dump, 0)
            KeyEvent.KEYCODE_F4 ->
                menus.performIdentifierAction(R.id.clear, 0)
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
            R.id.explorer -> {
                val text = input.text.toString()
                if (text.lines().size < 2) return false
                val jsonObject = JSONObject(text.lines()[1])
                val itemArray = jsonObject.getJSONArray("itemData")
                val lists = arrayListOf<String>()
                for (i in 0 until itemArray.length()) {
                    val item = itemArray.getJSONObject(i)
                    val spawnId = item.getString("spawnId")
                    lists.add(spawnId)
                }
                dialog("Object Browser", null, null, {_,_->}, null, "Close", lists.toTypedArray()) {_, i ->

                }
            }
            R.id.mods -> {
                val globals = JsePlatform.standardGlobals()
                val utilClass = PCSimulatorSaveEditorUtilClass()
                utilClass.globalVars = globalVars
                utilClass.activity2 = this
                utilClass.edittext = input
                utilClass.menu = menus
                globals.load(utilClass)
                val file = File(filesDir.toURI())
                val directoryList = file.listFiles()
                val mods = arrayListOf<Mod>()
                val files = arrayListOf<File>()
                if (directoryList != null) {
                    for (fil in directoryList)
                        if (!fil.isDirectory && fil.extension ==  "pcssemod") {
                            val contents = JSONObject(readTextFromUri(fil.toUri()))
                            val mod = Mod(
                                name = contents.getString("name"),
                                description = contents.optString(
                                    "description",
                                    "No description provided."
                                ),
                                license = contents.optString("license", "No license provided"),
                                creator = contents.optString("author", "Anonymous creator"),
                                repositoryUrl = contents.optString("repository"),
                                version = contents.optString("version", "v1.0.0"),
                                source = contents.getString("source")
                            )
                            mods.add(mod)
                            files.add(fil)
                        }
                }
                val strArray = arrayListOf<String>()
                for (mod in mods.toTypedArray()) {
                    strArray.add(mod.name)
                }
                dialog("Mods", null, {_, _ ->
                    pickMod.launch(arrayOf("application/octet-stream"))
                }, {_,_->}, "Add Mod", "Close", strArray.toTypedArray()) {_, i ->
                    val dialog = ModFragment()
                    dialog.mod = mods[i]
                    dialog.show(supportFragmentManager, "modFragment")
                    dialog.deleteListener = {
                        files[i].delete()
                        dialog.dismiss()
                    }
                }
            }
            R.id.shortcuts -> if (Build.VERSION.SDK_INT >= 24) requestShowKeyboardShortcuts()
            R.id.decrypt_after_opening -> decrypt_after_opening = decrypt_after_opening.not()
            R.id.encrypt_after_saving -> encrypt_after_saving = encrypt_after_saving.not()
            R.id.changelog -> dialog("Changelog (version $version)", globalVars.ChangelogText, {_,_->}, null)
            R.id.about -> dialog("PC Simulator Save Editor Android Port (version $version)", """
                    |Created by Mokka Chocolata.
                    |Free, and open source.
                    |Get beta builds at the Actions tab at the GitHub repository.
                    |Report any issues at the Issues tab at the GitHub repository.
                    |This app is licensed with GPLv3.0 or later.
                    |This project is neither associated, affiliated, nor endorsed by Intel or AMD. 
                    |""".trimMargin(), {_,_->}, null // This way we won't get into trouble
            )
            R.id.help -> startActivity(Intent(applicationContext, HelpActivity::class.java))
            R.id.repo -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/BeboKhouja/PCSimulatorSaveEditorAndroidPort")))
            R.id.discord -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/GXRECJjhVr")))
            R.id.clear -> {
                // Clear all cardboard boxes
                val text = input.text.toString()
                val lines = text.lines()
                val jsonObject = JSONObject(lines[1])
                val itemArray = jsonObject.getJSONArray("itemData")
                for (i in 0 until itemArray.length()) if (i < itemArray.length() && (itemArray.getJSONObject(i).getString("spawnId") == "CardboardBox" || itemArray.getJSONObject(i).getString("spawnId") == "LongCardboardBox" || itemArray.getJSONObject(i).getString("spawnId") == "CardboardBox 2")) itemArray.remove(i)
                for (i in 0 until itemArray.length()) if (i < itemArray.length() && (itemArray.getJSONObject(i).getString("spawnId").contains("Part_") || itemArray.getJSONObject(i).getString("spawnId") == "Part")) itemArray.remove(i)
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
                            if (password.isNotEmpty()) pwd += "$password : $spawnId \n"
                        } else {
                            // Assume we use a different way to do this
                            val password = data.getString("password")
                            if (password.isNotEmpty()) pwd += "$password : $spawnId\n"
                        }
                    }
                }
                dialog("Result", pwd, {_,_->}, null)
            }
            R.id.insert -> {
                val text = input.text.toString()
                val jsonObject = JSONObject(text.lines()[1])
                val lines = text.lines()
                val itemArray = jsonObject.getJSONArray("itemData")
                val position = Position(
                    (jsonObject.get("playerData") as JSONObject).getDouble("x"),
                    (jsonObject.get("playerData") as JSONObject).getDouble("y"),
                    (jsonObject.get("playerData") as JSONObject).getDouble("z")
                )
                val itemListJson = arrayListOf<String>()
                val inputStream = assets.open("insertJSON.json").bufferedReader().readText()
                val json = JSONArray(inputStream)
                fun getKeyFromString(key: String): String {
                    val output = getString(R.string::class.members.first { it.name == key.drop(1) }.call() as Int)
                    return output
                }
                fun addObjectFromJson(json: JSONObject) {
                    var name = json.getString("name")
                    if (name.startsWith("@")) name = getKeyFromString(name)
                    itemListJson.add(name)
                }
                fun insertObject(item: Any) {
                    itemArray.put(item)
                    input.setText(lines[0] + "\n" + jsonObject.toString())
                }
                fun handleClickJson(index : Int) {
                    fun putFile(i: Int, array: JSONArray) {
                        when(i) {
                            17 -> array.put(FileObjectJson(fileList[i].path, "pcos", true, 0, 0))
                            else -> array.put(FileObjectJson(fileList[i].path, "", false, 0, 0))
                        }
                    }

                    var jsonObj : ObjectJson
                    val obj = json.getJSONObject(index)
                    val additionals = obj.getJSONObject("additional")
                    val action = obj.getJSONObject("action")
                    when (obj.getString("type")) {
                        "dialogEditText" -> {
                            val edittext = EditText(this)
                            dialog(itemListJson[index], additionals.optString("message"), {_, _ ->
                                jsonObj = ObjectJson(if (!action.isNull("property")) action.optString("property") else edittext.text.toString(), (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), null)
                                insertObject(jsonObj.toJson())
                            }, {_,_->}, edittext)
                        }
                        "market" -> {
                            val marketJson = arrayListOf<String>()
                            for (i in 0 until action.getJSONArray("list").length()) {
                                var name = action.getJSONArray("list").getJSONObject(i).getString("name")
                                if (name.startsWith("@")) name = getKeyFromString(name)
                                marketJson.add(name)
                            }
                            var name = obj.getString("name")
                            if (name.startsWith("@")) name = getKeyFromString(name)
                            dialog(name, null, null, null, marketJson.toTypedArray()) {_, i ->
                                val random = (0..2147483647).random()
                                insertObject(ObjectJson(if (action.has("prefix")) action.getString("prefix") + action.getJSONArray("list").getJSONObject(i).getString("spawnId") else action.getJSONArray("list").getJSONObject(i).getString("spawnId"), random, position, Rotation(0.0, 0.0, 0.0, 0.0), if (action.getJSONArray("list").getJSONObject(i).has("customData")) action.getJSONArray("list").getJSONObject(i).getJSONObject("customData") else null).toJson())
                            }
                        }
                        "drive" -> {
                            var driveName : String
                            val driveType = additionals.getString("driveType")
                            val boolArray = BooleanArray(fileList.size)
                            var password : String
                            val edittext = EditText(this)
                            val dialogDriveName : String
                            val size = when (driveType) {
                                "usb" -> {
                                    dialogDriveName = "USB"
                                    emptyArray()
                                }
                                "ssd" -> {
                                    dialogDriveName = "SATA SSD"
                                    arrayOf("128GB", "256GB", "512GB", "1TB", "2TB")
                                }
                                "nvme" -> {
                                    dialogDriveName = "M.2 NVMe SSD"
                                    arrayOf("128GB", "256GB", "512GB", "1TB")
                                }
                                "hdd" -> {
                                    dialogDriveName = "HDD"
                                    arrayOf("500GB", "1TB", "2TB", "5TB")
                                }
                                else -> throw UnsupportedOperationException("That's not a supported type!")
                            }
                            dialog("$dialogDriveName Drive Name", "Set the storage name that appears in Disk Management and when you hold it.", { _, _ ->
                                driveName = edittext.text.toString()
                                val edittextUSB = EditText(this)
                                edittextUSB.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                dialog("Password", "Set the password of this drive.", { _, _ ->
                                    password = edittextUSB.text.toString()
                                    // Null the message out because the items wont appear
                                    if (size.isEmpty()) {
                                        dialogMultiChoice("Files", null, {_, _ ->
                                            val array = JSONArray()
                                            for (checked in boolArray.indices) if (boolArray[checked]) putFile(checked, array)
                                            val drive = USBObjectJson((0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array)
                                            insertObject(drive.toJson())
                                        }, null, fileList.map {it.name}.toTypedArray(), {_, which, isChecked ->
                                            boolArray[which] = isChecked
                                        }, boolArray)
                                    } else {
                                        dialog("$dialogDriveName Size", null, null,  null, size) {_, i ->
                                            val driveSize = size[i]
                                            dialogMultiChoice("Files", null, {_, _ ->
                                                val array = JSONArray()
                                                for (checked in boolArray.indices) {
                                                    if (boolArray[checked]) {
                                                        putFile(checked, array)
                                                    }
                                                }
                                                val thisdrive : String = when (driveType) {
                                                    "ssd" -> "SSD"
                                                    "nvme" -> "SSD_M.2"
                                                    "hdd" -> "HDD"
                                                    else -> throw UnsupportedOperationException("Not a valid DriveType")
                                                }
                                                val drive = DriveObjectJson(thisdrive, driveSize, (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), driveName, password, 0.0, 100.0, array, "User")
                                                insertObject(drive.toJson())
                                            }, null, fileList.map {it.name}.toTypedArray(), {_, which, isChecked ->
                                                boolArray[which] = isChecked
                                            }, boolArray)
                                        }
                                    }
                                }, {_,_->}, edittextUSB)
                            }, {_,_->}, edittext)
                        }
                        "banner" -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        "nothing" -> {
                            jsonObj = ObjectJson(action.getString("property"), (0..2147483647).random(), position, Rotation(0.0,0.0,0.0,0.0), null)
                            itemArray.put(jsonObj.toJson())
                            input.setText(lines[0] + "\n" + jsonObject.toString())
                        }
                    }
                }
                for (i in 0 until json.length())
                    addObjectFromJson(json.getJSONObject(i))
                dialog(resources.getString(R.string.insert), null, null, null, itemListJson.toTypedArray()) { _, i ->
                    handleClickJson(i)
                }
            }
            R.id.license -> {
                dialog("Open Source Licenses", """
                    This app is licensed with GPLv3.0 or later.
                    AndroidX (Apache License 2.0)
                    Android (Apache License 2.0)
                    JSONJava (Public Domain)
                """.trimIndent(), {_,_->}, null)
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
                val init = lines[0].isEmpty()
                if (init) return false
                jsonObject = JSONObject(lines[0])
                dialog("Save Options", null, null, null, optionList) {_, i ->
                    fun doItEdittext(title: String, message: String, propertyName: String, int: Boolean, long: Boolean) {
                        val edittext = EditText(this)
                        if (int or long) {
                            edittext.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                        }
                        dialog(title, message ,{_, _ ->
                            if (init) {
                                if (int) jsonObject.put(propertyName, parseInt(edittext.text.toString())) else if (long) jsonObject.put(propertyName, parseLong(edittext.text.toString())) else jsonObject.put(propertyName, edittext.text)
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
                        0 -> doItEdittext("AC Temperature", "Set the temperature of the AC. Max 2147483647 and min -2147483648", "temperature",  true, false)
                        1 -> doItSwitch("AC Power", "Switch on or off the AC", "ac",  "Power")
                        2 -> doItEdittext("Version", "Set the version of the save.", "version",  false, false)
                        3 -> doItEdittext("Money", "Set the money of the save. Max 2147483647 and min -2147483647", "coin",  true, false)
                        4 -> doItEdittext("Room", "Set the current room.\n0: Medium\n1: Large\n2: Double Storey\n3: Factory.", "room",  true, false)
                        5 -> doItSwitch("Gravity", "Switch on or off gravity.", "gravity", "Gravity")
                        6 -> doItSwitch("Hardcore", "Switch on or off hardcore mode.", "hardcore", "Hardcore")
                        7 -> doItEdittext("Playtime", "Set the current playtime.", "playtime", false, true)
                        8 -> doItSwitch("Light", "Switch on or off the lamp.", "light", "Light")
                        9 -> doItEdittext("Sign", "Set the signer of the save.", "sign", false, false)
                        10 -> doItEdittext("Save name", "Set the name of the save.", "roomName", false, false)
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

    private fun loadScripts() {
        val globals = JsePlatform.standardGlobals()
        val utilClass = PCSimulatorSaveEditorUtilClass()
        utilClass.globalVars = globalVars
        utilClass.activity2 = this
        utilClass.edittext = input
        utilClass.menu = menus
        globals.load(utilClass)
        val file = File(filesDir.toURI())
        val directoryList = file.listFiles()
        if (directoryList != null) {
            for (fil in directoryList) {
                if (!fil.isDirectory && fil.extension ==  "pcssemod") {
                    val contents = JSONObject(readTextFromUri(fil.toUri()))
                    val mod = Mod(
                        name = contents.getString("name"),
                        description = contents.optString(
                            "description",
                            "No description provided."
                        ),
                        license = contents.optString("license", "No license provided"),
                        creator = contents.optString("author", "Anonymous creator"),
                        repositoryUrl = contents.optString("repository"),
                        version = contents.optString("version", "v1.0.0"),
                        source = contents.getString("source")
                    )
                    globals.load(mod.source).call()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        if (menu != null) menus = menu
        if (Build.VERSION.SDK_INT <= 24) menu?.findItem(R.id.shortcuts)?.isEnabled = false
        loadScripts()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 31) installSplashScreen()
        globalVars = GlobalVars(resources)
        ChangelogText = globalVars.ChangelogText
        version = globalVars.version
        enableEdgeToEdge()
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
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
                        val fileItem = event.clipData.getItemAt(0)
                        val uri = fileItem.uri
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

                    else -> return@setOnDragListener false
                }
            }
        }


        decrypt.setOnClickListener { _ ->
            if (input.text.isNotEmpty()) input.setText(functions.Decrypt(input.text.toString()))
        }

        val open = findViewById<Button>(R.id.open)
        save = findViewById(R.id.save)
        val copy = findViewById<Button>(R.id.clipboard)

        open.setOnClickListener { _ -> pickFile.launch(arrayOf("application/octet-stream")) }

        save.setOnClickListener {_ ->
            System.gc()
            if (input.text.isNotEmpty()) {
                saveString = if (encrypt_after_saving) functions.Decrypt(input.text.toString()) else input.text.toString()
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
            if (input.text.isNotEmpty()) getSystemService(this, ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("PC Simulator Save", input.text.toString()))
        }

        val appLinkIntent: Intent = intent
        val appLinkAction: String? = appLinkIntent.action
        val appLinkData: Uri? = appLinkIntent.data
        if (Intent.ACTION_VIEW == appLinkAction) appLinkData?.lastPathSegment?.also { _ ->
            if (decrypt_after_opening) input.setText(functions.Decrypt(readTextFromUri(appLinkData))) else input.setText(readTextFromUri(appLinkData))
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

    private fun doOnThread(obj: Runnable) {
        val actualThread = Thread(obj)
        actualThread.start()
        actualThread.join()
    }

    private fun readTextFromUri(uri: Uri): String {
        val uriThread = ReadTextFromUriThread()
        uriThread.resolver = resolver
        uriThread.uri = uri
        doOnThread(uriThread)
        return uriThread.output
    }

    override fun run() {
        if (!WriteOrRead) {
            functions.input = readTextFromUri(data)
            doOnThread(functions)
            if (decrypt_after_opening) input.setText(functions.Output) else input.setText(readTextFromUri(data))
        } else if (saveToTxt) {
            functions.input = readTextFromUri(data)
            doOnThread(functions)
            clazz.saveString = functions.Output
            clazz.saveTheFile.launch("Save.txt")
        }
    }
}

class AfterReadThread : Runnable{
    lateinit var afterData : Uri
    lateinit var resolver : ContentResolver
    lateinit var text: String

    override fun run() {
        try {
            resolver.openFileDescriptor(afterData, "w")?.use { it ->
                val outputstream = FileOutputStream(it.fileDescriptor)
                outputstream.use { it.write(text.toByteArray()) }
                outputstream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
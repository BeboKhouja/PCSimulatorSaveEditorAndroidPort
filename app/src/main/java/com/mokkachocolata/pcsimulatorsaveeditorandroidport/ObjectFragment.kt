package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import org.json.JSONObject
import java.lang.Long.parseLong

class ObjectFragment(val obj: JSONObject, val textBox: EditText, val index: Int) : DialogFragment() {
    lateinit var aktivity: MainActivity2
    private fun doItEdittext(title: String, message: String, propertyName: String, long: Boolean) {
        val edittext = EditText(aktivity)
        if (long) {
            edittext.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        edittext.setText(obj.get(propertyName).toString())
        aktivity.dialog(title, message ,{_, _ ->
            val text = textBox.text.toString()
            val lines = text.lines()
            val jsonObject = JSONObject(lines[1])
            val itemArray = jsonObject.getJSONArray("itemData")
            val itemObject = itemArray.getJSONObject(index)
            itemObject.put(propertyName, if (long) parseLong(edittext.text.toString()) else edittext.text.toString())
            textBox.setText(lines[0] + "\n" + jsonObject.toString())
            dismiss()
        } , {_, _ ->}, edittext)
    }
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        aktivity = activity as MainActivity2
        val flate = inflater.inflate(R.layout.fragment_object, container, false)
        val pos = obj.getJSONObject("pos")
        val rot = obj.getJSONObject("rot")
        flate.findViewById<TextView>(R.id.pos).text = "${pos.getDouble("x")}, ${pos.getDouble("y")}, ${pos.getDouble("z")}"
        flate.findViewById<TextView>(R.id.rotation).text = "${rot.getDouble("x")}, ${rot.getDouble("y")}, ${rot.getDouble("z")}, ${rot.getDouble("w")}"
        flate.findViewById<TextView>(R.id.smasnug).text = "ID: ${obj.getInt("id")}"
        flate.findViewById<TextView>(R.id.title).text = obj.getString("spawnId")
        flate.findViewById<Button>(R.id.delete).setOnClickListener {
            val text = textBox.text.toString()
            val lines = text.lines()
            val jsonObject = JSONObject(lines[1])
            val itemArray = jsonObject.getJSONArray("itemData")
            itemArray.remove(index)
            textBox.setText(lines[0] + "\n" + jsonObject.toString())
            dismiss()
        }
        flate.findViewById<Button>(R.id.setSpawnID).setOnClickListener {
            doItEdittext("Set spawn ID", "", "spawnId", false)
        }
        flate.findViewById<Button>(R.id.setID).setOnClickListener {
            doItEdittext("Set ID", "", "id", true)
        }
        if (!(obj.getString("spawnId").contains("SSD") or obj.getString("spawnId").contains("HDD") or obj.getString("spawnId").contains("SSD_M.2") or obj.getString("spawnId").contains("FlashDrive")))
            flate.findViewById<Button>(R.id.filexplorer).visibility = Button.GONE
        flate.findViewById<Button>(R.id.filexplorer).setOnClickListener {
            val lists = arrayListOf<String>()
            val data = obj.getJSONObject("data")
            for (file in data.getJSONArray("files"))
                lists.add(file.getString("path"))
            aktivity.dialog("File Explorer", "", null, {_,_->}, null, "Close", lists.toTypedArray()) {_, i ->

            }
        }
        return flate
    }

}
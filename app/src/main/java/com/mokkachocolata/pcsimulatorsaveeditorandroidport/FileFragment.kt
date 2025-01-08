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

class FileFragment(val obj: JSONObject, val textBox: EditText, val objindex : Int, val index: Int) : DialogFragment() {
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
            val itemObject = itemArray.getJSONObject(objindex)
            val file = itemObject.getJSONObject("data").getJSONArray("files").getJSONObject(index)
            file.put(propertyName, if (long) parseLong(edittext.text.toString()) else edittext.text.toString())
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
        val text = textBox.text.toString()
        val lines = text.lines()
        val jsonObject = JSONObject(lines[1])
        val itemArray = jsonObject.getJSONArray("itemData")
        val file = itemArray.getJSONObject(objindex).getJSONObject("data").getJSONArray("files").getJSONObject(index)
        val flate = inflater.inflate(R.layout.fragment_file, container, false)
        val size = file.getInt("size")
        val hidden = file.getBoolean("hidden")
        flate.findViewById<TextView>(R.id.size).text = size.toString()
        flate.findViewById<TextView>(R.id.hidden).text = if (hidden) "Hidden" else "Visible"
        flate.findViewById<TextView>(R.id.title).text = file.getString("path")
        flate.findViewById<Button>(R.id.delete).setOnClickListener {
            itemArray.getJSONObject(objindex).getJSONObject("data").getJSONArray("files").remove(index)
            textBox.setText(lines[0] + "\n" + jsonObject.toString())
            dismiss()
        }
        flate.findViewById<Button>(R.id.rename).setOnClickListener {
            doItEdittext("Rename", "", "spawnId", false)
        }
        flate.findViewById<Button>(R.id.dupe).setOnClickListener {
            val edittext = EditText(aktivity)
            edittext.setText(obj.get("path").toString())
            aktivity.dialog("Duplicate path", "",{_, _ ->
                val clone = JSONObject(file.toString())
                clone.put("path", edittext.text)
                itemArray.getJSONObject(objindex).getJSONObject("data").getJSONArray("files").put(clone)
                textBox.setText(lines[0] + "\n" + jsonObject.toString())
            }, {_,_->}, edittext)
        }
        flate.findViewById<Button>(R.id.makhidden).text = if (file.getBoolean("hidden")) "Make visible" else "Make hidden"
        flate.findViewById<Button>(R.id.makhidden).setOnClickListener {
            itemArray.getJSONObject(objindex).getJSONObject("data").getJSONArray("files").getJSONObject(index).put("hidden", !itemArray.getJSONObject(objindex).getJSONObject("data").getJSONArray("files").getJSONObject(index).getBoolean("hidden"))
            flate.findViewById<Button>(R.id.makhidden).text = if (file.getBoolean("hidden")) "Make visible" else "Make hidden"
            textBox.setText(lines[0] + "\n" + jsonObject.toString())
        }
        return flate
    }

}
package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import org.json.JSONArray
import org.json.JSONObject

class ObjectFragment(val obj: JSONObject, val textBox: EditText, val index: Int) : DialogFragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
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
        return flate
    }

}
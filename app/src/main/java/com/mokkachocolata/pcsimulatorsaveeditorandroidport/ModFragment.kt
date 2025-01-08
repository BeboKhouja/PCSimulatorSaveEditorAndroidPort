package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ModFragment(val mod: MainActivity2.Mod) : DialogFragment() {
    var deleteListener: () -> Unit = {}
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val flate = inflater.inflate(R.layout.mod_fragment, container, false)
        flate.findViewById<TextView>(R.id.repo).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mod.repositoryUrl)))
        }
        if (mod.repositoryUrl?.isEmpty() == true) flate.findViewById<TextView>(R.id.repo).visibility = View.GONE
        flate.findViewById<TextView>(R.id.creator).text = mod.creator
        flate.findViewById<TextView>(R.id.title).text = mod.name
        flate.findViewById<TextView>(R.id.size).text = mod.description
        flate.findViewById<TextView>(R.id.hidden).text = mod.license
        flate.findViewById<TextView>(R.id.version).text = mod.version
        flate.findViewById<Button>(R.id.delete).setOnClickListener { deleteListener() }
        return flate
    }

}
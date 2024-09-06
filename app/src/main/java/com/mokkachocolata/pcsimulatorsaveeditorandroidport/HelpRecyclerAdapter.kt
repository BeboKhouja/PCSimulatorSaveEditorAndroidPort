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

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class HelpRecyclerAdapter(data : List<Url>): RecyclerView.Adapter<HelpRecyclerAdapter.ViewHolder>() {
    var datanew : List<Url>
    lateinit var thisholder : ViewHolder
    init {
        datanew = data
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_help, parent, false)
        val holder = ViewHolder(view)
        thisholder = holder
        return holder
    }

    fun filterList(filteredList: ArrayList<Url>) {
        datanew = filteredList
        notifyDataSetChanged()
    }

    fun destroyWebview() {
        thisholder.webpreview.destroy()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ItemsViewModel = datanew[position]
        holder.webpreview.loadUrl(ItemsViewModel.url)
        val settings = holder.webpreview.settings
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        holder.textpreview.text = ItemsViewModel.name
        holder.invisibleButton.bringToFront()
        holder.invisibleButton.setOnClickListener{_ ->
            val intent = Intent(holder.context, BrowserActivity::class.java)
            intent.putExtra("index", ItemsViewModel.index)
            holder.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return datanew.size
    }
    class ViewHolder(ItemView : View) : RecyclerView.ViewHolder(ItemView) {
        val webpreview = itemView.findViewById<WebView>(R.id.webpreview)
        val textpreview = itemView.findViewById<TextView>(R.id.textpreview)
        val layout = itemView.findViewById<LinearLayout>(R.id.layout)
        // This is an invisible button over the webview and textbox so we can process click requests
        val invisibleButton = itemView.findViewById<View>(R.id.view4)
        val context = itemView.context
    }

}
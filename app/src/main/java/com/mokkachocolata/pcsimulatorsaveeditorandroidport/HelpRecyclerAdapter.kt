package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


public class HelpRecyclerAdapter(private val data : List<Url>): RecyclerView.Adapter<HelpRecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HelpRecyclerAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_help, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpRecyclerAdapter.ViewHolder, position: Int) {
        val ItemsViewModel = data[position]
        holder.webpreview.loadUrl(ItemsViewModel.url)
        holder.textpreview.text = ItemsViewModel.name

    }

    override fun getItemCount(): Int {
        return data.size
    }
    class ViewHolder(ItemView : View) : RecyclerView.ViewHolder(ItemView) {
        val webpreview = itemView.findViewById<WebView>(R.id.webpreview)
        val textpreview = itemView.findViewById<TextView>(R.id.textpreview)
    }

}
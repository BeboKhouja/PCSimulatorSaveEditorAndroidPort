package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale


class HelpActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var adapter: HelpRecyclerAdapter
    private lateinit var urlList: ArrayList<Url>
    private lateinit var globalVars : GlobalVars
    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<Url>()
        val emptylist = ArrayList<Url>()

        // running a for loop to compare elements.
        for (item in urlList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (!filteredlist.isEmpty()) {
            // at last we are passing that filtered
            // list to our adapter class.
            adapter.filterList(filteredlist)
        } else {
            // This is an empty list when nothing is found.
            adapter.filterList(emptylist)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.destroyWebview()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_help, menu)

        val searchItem = menu?.findItem(R.id.app_bar_search)
        val searchView = searchItem?.actionView as SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filter(newText)
                }
                return false
            }

        })

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalVars = GlobalVars(resources)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.urlList = globalVars.urlArrayArray

        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        recyclerView.layoutManager = layoutManager

        val data = ArrayList<Url>()


        for (item in urlList) {
            data.add(item)
        }
        val adapter = HelpRecyclerAdapter(data)
        this.adapter = adapter

        recyclerView.adapter = adapter


    }

    override fun onClick(view: View?) {
        
    }
}
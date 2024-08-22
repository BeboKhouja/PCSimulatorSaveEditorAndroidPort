package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.content.res.Resources

class GlobalVars(resources: Resources){

    public val version = "1.3.7"
    public val ChangelogText = """
            - Save options now added.
        """.trimIndent()
    public val urlArray = listOf(
        Url(resources.getString(R.string.about),0, "file:///android_asset/About.htm"),
        Url(resources.getString(R.string.term), 1, "file:///android_asset/Terminologies.htm"),
        Url(resources.getString(R.string.save_file_help), 2, "file:///android_asset/PC Simulator Save Files.htm"),
        Url(resources.getString(R.string.websites), 3, "file:///android_asset/PC Simulator Websites.htm"),
        Url(resources.getString(R.string.sourceintro), 4, "file:///android_asset/Source/PC Simulator source code introduction.htm"),
        Url("Yiming.AntiCheat", 5, "file:///android_asset/Source/Yiming.AntiCheat.htm"),
        Url(resources.getString(R.string.howtouse), 6, "file:///android_asset/How to use Android port.htm"),
        Url("Illegal items", 7, "file:///android_asset/Illegal items.htm"),
    )
    public val urlArrayArray = ArrayList<Url>()
    init {
        urlArray.toCollection(urlArrayArray)
    }
}
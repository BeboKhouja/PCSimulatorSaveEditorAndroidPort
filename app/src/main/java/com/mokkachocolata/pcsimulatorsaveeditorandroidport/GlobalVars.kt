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

import android.content.res.Resources
import androidx.annotation.Keep

@Keep
class GlobalVars(resources: Resources){
    val version = "1.4.4"
    val ChangelogText = """
        - Migrated to the JSON API for the Insert Object menu only.
        """.trimIndent()
    val urlArray = listOf(
        Url(resources.getString(R.string.about),0, "file:///android_asset/About.htm"),
        Url(resources.getString(R.string.term), 1, "file:///android_asset/Terminologies.htm"),
        Url(resources.getString(R.string.save_file_help), 2, "file:///android_asset/PC Simulator Save Files.htm"),
        Url(resources.getString(R.string.websites), 3, "file:///android_asset/PC Simulator Websites.htm"),
        Url(resources.getString(R.string.sourceintro), 4, "file:///android_asset/Source/PC Simulator source code introduction.htm"),
        Url("Yiming.AntiCheat", 5, "file:///android_asset/Source/Yiming.AntiCheat.htm"),
        Url(resources.getString(R.string.howtouse), 6, "file:///android_asset/How to use Android port.htm"),
        Url(resources.getString(R.string.illegalitems), 7, "file:///android_asset/Illegal items.htm"),
        Url(resources.getString(R.string.howtoedit), 8, "file:///android_asset/How to edit PC Simulator Save Files.htm")
    )
    val urlArrayArray = ArrayList<Url>()
    init {
        urlArray.toCollection(urlArrayArray)
    }
}
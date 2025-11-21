/**
 * PC Simulator Save Editor is a free and open source save editor for PC Simulator.
 *     Copyright (C) 2025  Mokka Chocolata
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

import org.json.JSONArray
import org.json.JSONObject


data class Position(val x : Double, val y: Double, val z: Double)

data class Rotation(val w : Double, val x : Double, val y : Double, val z : Double)

// SpawnId can be one of these:
// Pillow
// Cube
// RTX4080Ti
// Projector

open class ObjectJson(val spawnId : String, val id : Int = 1 /* In PC Simulator ID is only used for display */, val pos : Position, val rot : Rotation, val data: JSONObject = JSONObject().also { it.put("glue", false) /* Every item has one even if its not a PC component */ }) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("spawnId", spawnId)
        jsonObject.put("id", id)
        jsonObject.put("pos", JSONObject().apply {
            put("x", pos.x)
            put("y", pos.y)
            put("z", pos.z)
        })
        jsonObject.put("rot", JSONObject().apply {
            put("x", rot.x)
            put("y", rot.y)
            put("z", rot.z)
            put("w", rot.w)
        })
        jsonObject.put("data", data)
        return jsonObject
    }

    override fun toString(): String {
        return this.toJson().toString()
    }
}

class PictureObjectJson(spawnId : String, pos : Position, rot : Rotation, bannerData : String):
    ObjectJson(spawnId, 1, pos, rot) {
    init {
        data.put("dat", bannerData)
    }
}

class USBObjectJson(
    id : Int,
    pos : Position,
    rot : Rotation,
    uptime : Double,
    health : Double,
    val files : JSONArray): ObjectJson("FlashDrive", id, pos, rot) {
    init {
        data.put("storageName", "Local Disk")
        data.put("password", "")
        data.put("files", files)
        data.put("uptime", uptime)
        data.put("health", health)
    }
}


data class FileObjectJson(
    val path : String,
    val content : String,
    val hidden : Boolean,
    val size : Long,
) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("path", path)
        jsonObject.put("content", content)
        jsonObject.put("hidden", hidden)
        jsonObject.put("size", size)
        return jsonObject
    }
}

class DriveObjectJson(
    driveType : String,
    storageSize : String,
    id : Int,
    pos : Position,
    rot : Rotation,
    uptime : Double,
    health : Double,
    val files : JSONArray,
): ObjectJson("$driveType $storageSize", id, pos, rot) {
    init {
        data.put("storageName", "Local Disk")
        data.put("password", "")
        data.put("files", files)
        data.put("uptime", uptime)
        data.put("health", health)
        data.put("damaged", false)
    }
}
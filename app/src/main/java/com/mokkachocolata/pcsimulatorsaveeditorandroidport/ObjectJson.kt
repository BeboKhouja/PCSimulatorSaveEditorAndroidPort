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

import androidx.annotation.Keep
import org.json.JSONArray
import org.json.JSONObject

@Keep
data class Position(val x : Double, val y: Double, val z: Double)
@Keep
data class Rotation(val w : Double, val x : Double, val y : Double, val z : Double)

// SpawnId can be one of these:
// Pillow
// Cube
// RTX4080Ti
// Projector
@Keep
data class ObjectJson(val SpawnId : String, val id : Int, val pos : Position, val rot : Rotation) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("spawnId", SpawnId)
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
        // Data not needed here so leave it empty
        jsonObject.put("data", JSONObject())
        return jsonObject
    }

    override fun toString(): String {
        return this.toJson().toString()
    }
}
@Keep
data class BannerObjectJson(val SpawnId : String, val id : Int, val pos : Position, val rot : Rotation, val bannerData : String) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("spawnId", "BannerStand")
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
        // Data not needed here so leave it empty
        val data = JSONObject()
        data.put("glue", false)
        data.put("dat", bannerData)
        jsonObject.put("data", data)
        return jsonObject
    }
}
@Keep
data class USBObjectJson(
    val id : Int,
    val pos : Position,
    val rot : Rotation,
    val storageName : String,
    val password: String,
    val uptime : Double,
    val health : Double,
    val files : JSONArray) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("spawnId", "FlashDrive")
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
        // Data not needed here so leave it empty
        val data = JSONObject()
        data.put("storageName", storageName)
        data.put("password", password)
        data.put("files", files)
        data.put("uptime", uptime)
        data.put("health", health)
        jsonObject.put("data", data)
        return jsonObject
    }
}

@Keep
data class FileObjectJson(
    val path : String,
    val content : String,
    val hidden : Boolean,
    val size : Long,
    val StorageSize : Long) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("path", path)
        jsonObject.put("content", content)
        jsonObject.put("hidden", hidden)
        jsonObject.put("size", size)
        jsonObject.put("StorageSize", StorageSize)
        return jsonObject
    }
}
@Keep
data class DriveObjectJson(
    val driveType : String,
    val storageSize : String,
val id : Int,
val pos : Position,
val rot : Rotation,
val storageName : String,
val password: String,
val uptime : Double,
val health : Double,
val files : JSONArray,
val username : String) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("spawnId", "$driveType $storageSize")
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
        val data = JSONObject()
        val storageData = JSONObject()
        storageData.put("storageName", storageName)
        storageData.put("userPassword", password)
        storageData.put("userPicturePath", "")
        storageData.put("userName", username)
        storageData.put("background", 0)
        storageData.put("files", files)
        data.put("storageData", storageData)
        data.put("uptime", uptime)
        data.put("health", health)
        data.put("damaged", false)
        jsonObject.put("data", data)
        return jsonObject
    }
}
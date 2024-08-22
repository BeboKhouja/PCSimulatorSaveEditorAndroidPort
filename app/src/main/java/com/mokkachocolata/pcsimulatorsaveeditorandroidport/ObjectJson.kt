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
class ObjectJson(val SpawnId : String, val id : Int, val pos : Position, val rot : Rotation) {
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
}

class BannerObjectJson(val SpawnId : String, val id : Int, val pos : Position, val rot : Rotation, val bannerData : String) {
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

class USBObjectJson(
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

class FileObjectJson(
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
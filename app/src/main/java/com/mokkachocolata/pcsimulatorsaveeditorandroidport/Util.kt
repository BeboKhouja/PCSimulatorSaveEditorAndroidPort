package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import org.json.JSONArray
import org.json.JSONObject

// Ported from Modrinth app
operator fun JSONArray.iterator() : Iterator<JSONObject> = (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()
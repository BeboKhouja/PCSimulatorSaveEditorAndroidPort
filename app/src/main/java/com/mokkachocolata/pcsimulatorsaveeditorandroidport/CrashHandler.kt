package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.os.Build
import android.util.Log
import kotlin.system.exitProcess

class CrashHandler : Thread.UncaughtExceptionHandler {
    private val seperator = "\n"
    private val LOG_TAG = "App"


    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e(LOG_TAG, "A fatal error has occured. The program will now close." + seperator + "Stack trace:")
        e.printStackTrace()
        Log.e(LOG_TAG, "Device info:$seperator")
        Log.e(LOG_TAG, "Device Manufacturer: " + Build.MANUFACTURER + seperator)
        Log.e(LOG_TAG, "Device Model: " + Build.MODEL + seperator)
        Log.e(LOG_TAG, "Android API: " + Build.VERSION.SDK_INT + seperator)
        Log.e(LOG_TAG, "RAM free at time of crash: " + Runtime.getRuntime().freeMemory())
        Log.e(LOG_TAG, "Device RAM: " + Runtime.getRuntime().totalMemory())

        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(-1)
    }
}
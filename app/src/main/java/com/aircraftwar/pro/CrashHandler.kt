package com.aircraftwar.pro

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {

        try {
            val time = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                .format(Date())

            val log = StringBuilder()

            log.append("=== AIRCRAFT WAR CRASH LOG ===\n")
            log.append("Time: $time\n")
            log.append("Thread: ${thread.name}\n\n")

            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))

            log.append(sw.toString())

            val file = File(context.filesDir, "crash_log.txt")
            file.appendText(log.toString() + "\n\n")

        } catch (e: Exception) {
            e.printStackTrace()
        }

        defaultHandler?.uncaughtException(thread, throwable)
    }
}

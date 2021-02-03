package com.example.weatherappbyssm.Common

import android.content.Context

class FilesWorker {

    fun writeLinesToFile(context: Context, line: String) {
        context.openFileOutput(Constants.fileName, Context.MODE_PRIVATE).use { output ->
            output.write(line.toByteArray())
        }
    }

    fun readLinesFromFile(context: Context): List<String> {
        var listFromFile: List<String>
        context.openFileInput(Constants.fileName).use { stream ->
            listFromFile = stream.bufferedReader().use {
                it.readLines()
            }
        }
        return listFromFile
    }
}
package com.example.weatherappbyssm.Common

import android.content.Context

class FilesWorker {

    fun writeLineToFile(context: Context, fileName: String, line: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
            output.write(line.toByteArray())
        }
    }

    fun readLinesFromFile(context: Context, fileName: String): List<String> {
        var dataFromFileList: List<String>
        context.openFileInput(fileName).use { stream ->
            dataFromFileList = stream.bufferedReader().use {
                it.readLines()
            }
        }
        return dataFromFileList
    }
}
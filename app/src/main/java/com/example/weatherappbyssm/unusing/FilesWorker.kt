package com.example.weatherappbyssm.unusing

import android.content.Context

class FilesWorker {

    private lateinit var dataFromFileList: List<String>

    /**Запись в файл построчно, без повторов*/
    fun writeLineToFile(context: Context, fileName: String, line: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE or Context.MODE_APPEND)
            .use { output ->
                dataFromFileList = readLinesFromFile(context, fileName)
                if (!dataFromFileList.contains(line))
                    output.write("${line}\n".toByteArray())
            }
    }

    /** Запись списка в файл*/
    fun writeLinesToFile(context: Context, fileName: String, list: List<String>) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE or Context.MODE_APPEND)
            .use { output ->
                output.write(list.toString().toByteArray())
            }
    }

    /**Чтение всех строк из файла*/
    fun readLinesFromFile(context: Context, fileName: String): List<String> {
        context.openFileInput(fileName).use { stream ->
            dataFromFileList = stream.bufferedReader().use {
                it.readLines()
            }
        }
        return dataFromFileList
    }
}
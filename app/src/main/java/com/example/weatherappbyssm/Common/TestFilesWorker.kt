package com.example.weatherappbyssm.Common

import java.io.FileWriter
import java.lang.Exception

fun main() {
    writeToFile("line")
}

fun writeToFile(line: String) {
    try {
        var fileWriter = FileWriter("test.txt")
        fileWriter.write(line)
        fileWriter.close()
    }catch (ex:Exception){
        print(ex.message)
    }
}
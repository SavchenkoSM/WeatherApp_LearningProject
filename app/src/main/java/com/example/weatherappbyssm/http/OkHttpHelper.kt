package com.example.weatherappbyssm.http

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Класс для формирования GET-запроса к API (основан на библиотеке OkHttp)
 */
class OkHttpHelper {

    /**
     * Парсинг полученных данных
     */
    private fun parseResponse(response: Response): Any {
        return response.body?.string() ?: ""
    }

    /**
     * Построение запроса
     */
    private fun createRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .build()
    }

    /**
     * Выполнение запроса
     */
    fun makeRequest(url: String): String {
        val okHttpClient = OkHttpClient()
        val parsedResponse = parseResponse(okHttpClient.newCall(createRequest(url)).execute())

        return parsedResponse.toString()
    }
}
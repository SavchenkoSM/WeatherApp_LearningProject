package com.example.weatherappbyssm.Common

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class OkHttpHelper {
    private fun parseResponse(response: Response): Any {
        return response.body?.string() ?: ""
    }

    private fun createRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .build()
    }

    fun makeRequest(url: String): String {
        val okHttpClient = OkHttpClient()
        val parsedResponse = parseResponse(okHttpClient.newCall(createRequest(url)).execute())

        return parsedResponse.toString()
    }
}
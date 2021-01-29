package com.example.weatherappbyssm.Common

import java.text.SimpleDateFormat
import java.util.*

object CommonObject {
    val API_KEY = "bca7c80097ffb145abd02c64c80b8bef"
    val API_URL = "https://api.openweathermap.org/data/2.5/"

    val currentDate: String
        get() {
            val dateFormat = SimpleDateFormat("dd M yyyy HH:mm")
            val date = Date()

            return dateFormat.format(date)
        }

    //Запрос к OpenWeatherApi по координатам (получение данных о текущей погоде)
    fun apiRequestCurrentWeather(lat: String, lng: String): String {
        var stringBuilder = StringBuilder(API_URL)
        stringBuilder.append("weather?lat=${lat}&lon=${lng}&APPID=${API_KEY}&units=metric")

        return stringBuilder.toString()
    }

    //Запрос к OpenWeatherApi по координатам (получение 5-дневного прогноза погоды)
    fun apiRequestWeatherForecast(lat: String, lng: String): String {
        var stringBuilder = StringBuilder(API_URL)
        stringBuilder.append("forecast?lat=${lat}&lon=${lng}&APPID=${API_KEY}&units=metric")

        return stringBuilder.toString()
    }


    //Получение изображения для текущей погоды
    fun getWeatherImage(icon: String): String {
        return "https://openweathermap.org/img/wn/${icon}.png"
    }

    //Конвертирование Unix Time Stamp в Date Time
    fun unixTimeStampToDateTime(unixTimeStamp: Double): String {
        val dateFormat = SimpleDateFormat("HH:mm")
        val date = Date()
        date.time = unixTimeStamp.toLong() * 1000

        return dateFormat.format(date)
    }
}
package com.example.weatherappbyssm.common

import com.example.weatherappbyssm.common.ConstantsObject.API_KEY
import com.example.weatherappbyssm.common.ConstantsObject.API_URL
import java.text.SimpleDateFormat
import java.util.*

object CommonObject {

    // Вывод текущей даты по формату dd/MM/yyyy HH:mm
    val currentDate: String
        get() {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
            val date = Date()

            return dateFormat.format(date)
        }

    var newCityName: String? = ""
    var chosenCityName: String? = ""
    var isCityChosen = false

    /**
     * Запрос к OpenWeatherApi по координатам (получение данных о текущей погоде)
     */
    fun apiRequestCurrentWeatherByCoordinates(lat: Double?, lng: Double?): String {
        val stringBuilder = StringBuilder(API_URL)
        stringBuilder.append("weather?lat=${lat}&lon=${lng}&appid=${API_KEY}&units=metric")

        return stringBuilder.toString()
    }

    /**
     * Запрос к OpenWeatherApi по координатам (получение 5-дневного прогноза погоды)
     */
    fun apiRequestWeatherForecastByCoordinates(lat: String, lng: String): String {
        val stringBuilder = StringBuilder(API_URL)
        stringBuilder.append("forecast?lat=${lat}&lon=${lng}&appid=${API_KEY}&units=metric")

        return stringBuilder.toString()
    }

    /**
     * Запрос к OpenWeatherApi по названию города (получение данных о текущей погоде)
     */
    fun apiRequestCurrentWeatherByCityName(cityName: String): String {
        val stringBuilder = StringBuilder(API_URL)
        stringBuilder.append("weather?q=${cityName}&appid=${API_KEY}&units=metric")

        return stringBuilder.toString()
    }

    /**
     * Получение изображения для текущей погоды
     */
    fun getWeatherImage(icon: String): String {
        return "https://openweathermap.org/img/w/${icon}.png"
    }

    /**
     * Конвертирование UnixTimeStamp в DateTime
     */
    fun unixTimeStampToDateTime(unixTimeStamp: Double): String {
        val dateFormat = SimpleDateFormat("HH:mm")
        val date = Date()
        date.time = unixTimeStamp.toLong() * 1000

        return dateFormat.format(date)
    }
}
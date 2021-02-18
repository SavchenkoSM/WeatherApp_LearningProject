package com.example.weatherappbyssm.common

/**
 * Объект, содержащий набор данных о погоде для отображения
 */
object WeatherDataForDisplay {
    var cityName: String? = null
    var country: String? = null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var skyStatus: String? = null
    var currentTemp: Double = 0.0
    var tempFeelsLike: Double = 0.0
    var lastWeatherUpdateTime: String = ""
    var windSpeed: Double = 0.0
    var pressure: Double = 0.0
    var humidity: Int = 0
    var minTemp: Double = 0.0
    var maxTemp: Double = 0.0
    var sunriseTime: String = ""
    var sunsetTime: String = ""
}
package com.example.weatherappbyssm.models

class Clouds(val all: Int)

class Coord(var lat: Double, var lon: Double)

class Main(
    var temp: Double,
    var feels_like: Double,
    var temp_min: Double,
    var temp_max: Double,
    var pressure: Double,
    var humidity: Int
)

class Rain(var _1h: Double, var _3h: Double)

class Snow(var _1h: Double, var _3h: Double)

class Root {
    var coord: Coord? = null
    var weather: List<Weather>? = null
    var base: String? = null
    var main: Main? = null
    var wind: Wind? = null
    var rain: Rain? = null
    var clouds: Clouds? = null
    var dt: Int = 0
    var sys: Sys? = null
    var id: Int = 0
    var name: String? = null
    var cod: Int = 0

    constructor() {}

    constructor(
        coord: Coord, weatherList: List<Weather>, base: String, main: Main,
        wind: Wind, rain: Rain, clouds: Clouds, dt: Int, name: String, cod: Int
    ) {
        this.coord = coord
        this.weather = weatherList
        this.base = base
        this.main = main
        this.wind = wind
        this.rain = rain
        this.clouds = clouds
        this.dt = dt
        this.sys = sys
        this.id = id
        this.name = name
        this.cod = cod
    }
}

class Sys(
    var type: Int,
    var id: Int,
    var message: Double,
    var country: String?,
    var sunrise: Double,
    var sunset: Double
)

class Weather(var id: Int, var main: String?, var description: String?, var icon: String?)

class Wind(var speed: Double, var deg: Double, var gust: Double)
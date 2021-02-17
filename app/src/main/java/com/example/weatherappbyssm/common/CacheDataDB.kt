package com.example.weatherappbyssm.common

import android.content.ContentValues
import android.content.Context

class CacheDataDB(context: Context) : DBHelper(context) {

    companion object {
        //Описание структуры таблицы
        private const val CACHE_TABLE_NAME = "cacheTable"
        private const val COLUMN_CITY_NAME = "cityName"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_SKY_STATUS = "skyStatus"
        private const val COLUMN_CURRENT_TEMP = "currentTemp"
        private const val COLUMN_TEMP_FEELS_LIKE = "tempFeelsLike"
        private const val COLUMN_LAST_WEATHER_UPDATE_TIME = "lastWeatherUpdateTime"
        private const val COLUMN_WIND_SPEED = "windSpeed"
        private const val COLUMN_PRESSURE = "pressure"
        private const val COLUMN_HUMINDITY = "humindity"
        private const val COLUMN_MIN_TEMP = "minTemp"
        private const val COLUMN_MAX_TEMP = "maxTemp"
        private const val COLUMN_SUNRISE_TIME = "sunriseTime"
        private const val COLUMN_SUNSET_TIME = "sunsetTime"

        //Запросы к БД
        const val CREATE_CACHE_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS $CACHE_TABLE_NAME " +
                    "($ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_CITY_NAME TEXT," +
                    "$COLUMN_COUNTRY TEXT," +
                    "$COLUMN_LATITUDE DOUBLE," +
                    "$COLUMN_LONGITUDE DOUBLE," +
                    "$COLUMN_SKY_STATUS TEXT," +
                    "$COLUMN_CURRENT_TEMP DOUBLE," +
                    "$COLUMN_TEMP_FEELS_LIKE DOUBLE," +
                    "$COLUMN_LAST_WEATHER_UPDATE_TIME TEXT," +
                    "$COLUMN_WIND_SPEED DOUBLE," +
                    "$COLUMN_PRESSURE DOUBLE," +
                    "$COLUMN_HUMINDITY INTEGER," +
                    "$COLUMN_MIN_TEMP DOUBLE," +
                    "$COLUMN_MAX_TEMP DOUBLE," +
                    "$COLUMN_SUNRISE_TIME TEXT," +
                    "$COLUMN_SUNSET_TIME TEXT)"
        const val INSERT_DEFAULT_ROW_QUERY = "INSERT INTO $CACHE_TABLE_NAME DEFAULT VALUES"
        private const val SELECT_ALL_FROM_CACHE_TABLE_QUERY = "SELECT * FROM $CACHE_TABLE_NAME"
        const val IS_ROW_NULL_QUERY = "IF $COLUMN_CITY_NAME IS NULL"
    }


    /**
     * Обновление закешированных данных
     */
    fun updateCacheDataInDB(
        cityName: String?,
        country: String?,
        latitude: Double,
        longitude: Double,
        skyStatus: String?,
        currentTemp: Double,
        tempFeelsLike: Double,
        lastWeatherUpdateTime: String,
        windSpeed: Double,
        pressure: Double,
        humindity: Int,
        minTemp: Double,
        maxTemp: Double,
        sunriseTime: String,
        sunsetTime: String
    ) {
        db = this.writableDatabase
        values = ContentValues()

        values.put(COLUMN_CITY_NAME, cityName)
        values.put(COLUMN_COUNTRY, country)
        values.put(COLUMN_LATITUDE, latitude)
        values.put(COLUMN_LONGITUDE, longitude)
        values.put(COLUMN_SKY_STATUS, skyStatus)
        values.put(COLUMN_CURRENT_TEMP, currentTemp)
        values.put(COLUMN_TEMP_FEELS_LIKE, tempFeelsLike)
        values.put(COLUMN_LAST_WEATHER_UPDATE_TIME, lastWeatherUpdateTime)
        values.put(COLUMN_WIND_SPEED, windSpeed)
        values.put(COLUMN_PRESSURE, pressure)
        values.put(COLUMN_HUMINDITY, humindity)
        values.put(COLUMN_MIN_TEMP, minTemp)
        values.put(COLUMN_MAX_TEMP, maxTemp)
        values.put(COLUMN_SUNRISE_TIME, sunriseTime)
        values.put(COLUMN_SUNSET_TIME, sunsetTime)

        db.update(CACHE_TABLE_NAME, values, "$ID=1", arrayOf())
    }

    /**
     * Чтение закешированных данных из БД
     */
    fun getCacheDataFromDB() {
        db = this.readableDatabase
        cursor = db.rawQuery(SELECT_ALL_FROM_CACHE_TABLE_QUERY, null)

        while (cursor.moveToNext()) {
            WeatherDataForDisplay.cityName =
                cursor.getString(cursor.getColumnIndex(COLUMN_CITY_NAME))
            WeatherDataForDisplay.country = cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY))
            WeatherDataForDisplay.latitude =
                cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE))
            WeatherDataForDisplay.longitude =
                cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))
            WeatherDataForDisplay.skyStatus =
                cursor.getString(cursor.getColumnIndex(COLUMN_SKY_STATUS))
            WeatherDataForDisplay.currentTemp =
                cursor.getDouble(cursor.getColumnIndex(COLUMN_CURRENT_TEMP))
            WeatherDataForDisplay.tempFeelsLike =
                cursor.getDouble(cursor.getColumnIndex(COLUMN_TEMP_FEELS_LIKE))
            WeatherDataForDisplay.lastWeatherUpdateTime =
                cursor.getString(cursor.getColumnIndex(COLUMN_LAST_WEATHER_UPDATE_TIME))
            WeatherDataForDisplay.windSpeed =
                cursor.getDouble(cursor.getColumnIndex(COLUMN_WIND_SPEED))
            WeatherDataForDisplay.pressure =
                cursor.getDouble(cursor.getColumnIndex(COLUMN_PRESSURE))
            WeatherDataForDisplay.humindity = cursor.getInt(cursor.getColumnIndex(COLUMN_HUMINDITY))
            WeatherDataForDisplay.minTemp = cursor.getDouble(cursor.getColumnIndex(COLUMN_MIN_TEMP))
            WeatherDataForDisplay.maxTemp = cursor.getDouble(cursor.getColumnIndex(COLUMN_MAX_TEMP))
            WeatherDataForDisplay.sunriseTime =
                cursor.getString(cursor.getColumnIndex(COLUMN_SUNRISE_TIME))
            WeatherDataForDisplay.sunsetTime =
                cursor.getString(cursor.getColumnIndex(COLUMN_SUNSET_TIME))
        }
        cursor.close()
        db.close()
    }
}
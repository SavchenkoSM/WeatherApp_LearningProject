package com.example.weatherappbyssm.common

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import com.example.weatherappbyssm.R

class WorkWithCitiesTableFromDB(context: Context) : DBHelper(context) {

    private lateinit var citiesMutableList: MutableList<String>

    companion object{
        // Описание таблицы
        const val CITIES_TABLE_NAME = "cities"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_CITY_NAME = "CityName"

        // Запросы к БД
        const val CREATE_CITIES_TABLE =
            "CREATE TABLE IF NOT EXISTS $CITIES_TABLE_NAME ($COLUMN_ID Integer PRIMARY KEY, $COLUMN_CITY_NAME TEXT)"
        private const val SELECT_ALL_QUERY = "SELECT * FROM $CITIES_TABLE_NAME"
    }

    /**
     * Запись в БД списка городов по умолчанию
     */
    fun addDefaultCitiesListToDB(db: SQLiteDatabase?) {
        values = ContentValues()
        citiesMutableList = context.resources.getStringArray(R.array.addedCities).toMutableList()

        for (i in 0 until citiesMutableList.size) {
            values.put(COLUMN_CITY_NAME, citiesMutableList[i])
            db?.insert(CITIES_TABLE_NAME, null, values)
        }
    }

    /**
     * Запись нового города в БД, без повторов
     */
    fun addNewCityToDB(cityName: String) {
        citiesMutableList = getAllCitiesFromDB()
        db = this.writableDatabase
        values = ContentValues()

        if (!citiesMutableList.contains(cityName)) {
            values.put(COLUMN_CITY_NAME, cityName)
            db.insert(CITIES_TABLE_NAME, null, values)
            db.close()

            Toast.makeText(context, "$cityName added successfully", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Чтение из БД списка городов
     */
    fun getAllCitiesFromDB(): MutableList<String> {
        db = this.readableDatabase
        cursor = db.rawQuery(SELECT_ALL_QUERY, null)
        citiesMutableList = mutableListOf()

        if (cursor.moveToFirst())
            do
                citiesMutableList.add(cursor.getString(cursor.getColumnIndex(COLUMN_CITY_NAME)))
            while (cursor.moveToNext())

        cursor.close()
        db.close()

        return citiesMutableList
    }
}
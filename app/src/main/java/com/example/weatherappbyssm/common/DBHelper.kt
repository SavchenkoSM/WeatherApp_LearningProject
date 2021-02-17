package com.example.weatherappbyssm.common

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.weatherappbyssm.R

open class DBHelper(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    protected lateinit var db: SQLiteDatabase
    protected lateinit var values: ContentValues
    protected lateinit var cursor: Cursor

    private lateinit var citiesMutableList: MutableList<String>

    companion object {
        // Описание БД
        private const val DB_NAME = "WeatherDB"
        private const val DB_VERSION = 1

        // Описание таблиц
        const val TABLE_NAME = "cities"
        const val ID = "_id"
        const val CITY_NAME = "CityName"

        // Запросы к БД
        private const val CREATE_CITIES_TABLE =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($ID Integer PRIMARY KEY, $CITY_NAME TEXT)"

        private const val DROP_TABLE_QUERY = "DROP TABLE IF EXISTS $TABLE_NAME"
        private const val SELECT_ALL_QUERY = "SELECT * FROM $TABLE_NAME"
    }

    /**
     * Создание таблицы в БД и запись в неё списка городов по умолчанию
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_CITIES_TABLE)
        addDefaultCitiesListToDB(db)

        db?.execSQL(CacheDataDB.CREATE_CACHE_TABLE_QUERY)
        db?.execSQL(CacheDataDB.INSERT_DEFAULT_ROW_QUERY)
    }

    /**
     * Уничтожение таблицы с последующим созданием таблицы с обновленной структурой
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DROP_TABLE_QUERY)
        onCreate(db)
    }

    /**
     * Запись в БД списка городов по умолчанию
     */
    private fun addDefaultCitiesListToDB(db: SQLiteDatabase?) {
        values = ContentValues()
        citiesMutableList = context.resources.getStringArray(R.array.addedCities).toMutableList()

        for (i in 0 until citiesMutableList.size) {
            values.put(CITY_NAME, citiesMutableList[i])
            db?.insert(TABLE_NAME, null, values)
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
            values.put(CITY_NAME, cityName)
            db.insert(TABLE_NAME, null, values)
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
                citiesMutableList.add(cursor.getString(cursor.getColumnIndex(CITY_NAME)))
            while (cursor.moveToNext())

        cursor.close()
        db.close()

        return citiesMutableList
    }
}
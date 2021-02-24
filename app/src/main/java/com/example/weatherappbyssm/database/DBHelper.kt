package com.example.weatherappbyssm.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Класс для работы с БД
 */
open class DBHelper(protected val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    protected lateinit var db: SQLiteDatabase
    protected lateinit var values: ContentValues
    protected lateinit var cursor: Cursor

    companion object {
        // Описание БД
        private const val DB_NAME = "WeatherDB"
        private const val DB_VERSION = 1

        //Запросы к БД
        private const val DROP_CITIES_TABLE_QUERY =
            "DROP TABLE IF EXISTS ${WorkWithCitiesTableFromDB.CITIES_TABLE_NAME}"
        private const val DROP_CACHE_TABLE_QUERY =
            "DROP TABLE IF EXISTS ${WorkWithCacheTableFromDB.CACHE_TABLE_NAME}"
    }

    /**
     * Создание таблицы в БД и запись в неё списка городов по умолчанию
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(WorkWithCitiesTableFromDB.CREATE_CITIES_TABLE)
        WorkWithCitiesTableFromDB(context).addDefaultCitiesListToDB(db)

        db?.execSQL(WorkWithCacheTableFromDB.CREATE_CACHE_TABLE_QUERY)
        db?.execSQL(WorkWithCacheTableFromDB.INSERT_DEFAULT_ROW_TO_CACHE_TABLE_QUERY)
    }

    /**
     * Уничтожение таблицы с последующим созданием таблицы с обновленной структурой
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DROP_CITIES_TABLE_QUERY)
        db?.execSQL(DROP_CACHE_TABLE_QUERY)
        onCreate(db)
    }
}
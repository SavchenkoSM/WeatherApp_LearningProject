package com.example.weatherappbyssm.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherappbyssm.R
import com.example.weatherappbyssm.common.CommonObject
import com.example.weatherappbyssm.common.DBHelper
import kotlinx.android.synthetic.main.added_cities_activity.*

class AddedCitiesActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private var dbHelper: DBHelper? = null
    private var citiesMutableList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.added_cities_activity)

        dbHelper = DBHelper(this)

        // Добавление нового города в БД, если он там отсутсвует
        if (CommonObject.newCityName != "")
            dbHelper!!.addNewCityToDB(CommonObject.newCityName.toString())
        // Получение списка городов из БД
        citiesMutableList = dbHelper!!.getAllCitiesFromDB()
        // Сортировка списка городов по алфавиту
        citiesMutableList.sort()

        // Вывод полученного списка городов на экран
        val arrayAdapter = ArrayAdapter<String>(
            this,
            R.layout.cities_list_item,
            citiesMutableList
        )

        citiesListView.adapter = arrayAdapter
        citiesListView.onItemClickListener = this
    }

    // Обработка выбора города
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val chosenCityName: String = parent?.getItemAtPosition(position).toString()

        // Когда город выбран: установка флага в true и запись выбранного города в переменную
        CommonObject.isCityChosen = true
        CommonObject.chosenCityName = chosenCityName

        // Переход на главную активность (отображающую погоду для выбранного города)
        this.startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
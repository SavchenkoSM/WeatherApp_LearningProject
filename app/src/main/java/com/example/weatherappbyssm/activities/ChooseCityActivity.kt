package com.example.weatherappbyssm.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherappbyssm.R
import com.example.weatherappbyssm.common.CommonObject
import com.example.weatherappbyssm.database.DBHelper
import com.example.weatherappbyssm.database.WorkWithCitiesTableFromDB
import kotlinx.android.synthetic.main.added_cities_activity.*

/**
 * Класс активности для выбора города
 */
class ChooseCityActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private var dbHelper: DBHelper? = null
    private var citiesMutableList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.added_cities_activity)

        dbHelper = DBHelper(this)

        // Получение списка городов из БД
        citiesMutableList = WorkWithCitiesTableFromDB(this).getAllCitiesFromDB()
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

        // Отображение погоды для текущего города по нажатию
        currentCityTextView.setOnClickListener {
            CommonObject.isCityChosen = false
            startMainActivity()
        }
    }

    /**
     * Обработка выбора города
     */
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val chosenCityName: String = parent?.getItemAtPosition(position).toString()

        // Когда город выбран: установка флага в true и запись выбранного города в переменную
        CommonObject.isCityChosen = true
        CommonObject.chosenCityName = chosenCityName

        Toast.makeText(
            this,
            "Your choice: $chosenCityName",
            Toast.LENGTH_LONG
        ).show()

        startMainActivity()
    }

    /**
     *  Переход на главную активность (отображающую погоду для выбранного города)
     */
    private fun startMainActivity() {
        val addedCitiesActivityIntent = Intent(this, MainActivity::class.java)
        addedCitiesActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(addedCitiesActivityIntent)
    }
}
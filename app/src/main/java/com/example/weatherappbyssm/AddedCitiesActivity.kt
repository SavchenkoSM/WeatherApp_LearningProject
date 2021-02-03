package com.example.weatherappbyssm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherappbyssm.Common.CommonObject
import com.example.weatherappbyssm.Common.FilesWorker
import kotlinx.android.synthetic.main.added_cities_activity.*


class AddedCitiesActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private var citiesMutableList: MutableList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.added_cities_activity)

        citiesMutableList.addAll(resources.getStringArray(R.array.addedCities))

        FilesWorker().writeLinesToFile(this, CommonObject.cityName.toString())
        citiesMutableList.addAll(FilesWorker().readLinesFromFile(this))

        val arrayAdapter = ArrayAdapter<String>(
            this,
            R.layout.cities_list_item,
            citiesMutableList
        )

        citiesListView.adapter = arrayAdapter
        citiesListView.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val chosenCityName: String = parent?.getItemAtPosition(position).toString()

        CommonObject.cityName = chosenCityName
        CommonObject.isCityChosen = true

        //переход на главную активность (отображающую погоду)
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }
}
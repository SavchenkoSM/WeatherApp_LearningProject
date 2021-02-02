package com.example.weatherappbyssm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherappbyssm.Common.CommonObject
import kotlinx.android.synthetic.main.added_cities_activity.*


class AddedCitiesActivity : AppCompatActivity(), AdapterView.OnItemClickListener {


    var citiesList: MutableList<String> = ArrayList()
    //private val cities = arrayOf("Tokyo", "Moscow", "San-Francisco", "Vladivostok", "Durban")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.added_cities_activity)

        citiesList = arrayListOf("Tokyo", "Moscow", "San-Francisco", "Vladivostok", "Durban")
        citiesList.add(CommonObject.cityName.toString())

        val arrayAdapter = ArrayAdapter<String>(
            this,
            R.layout.cities_list_item,
            citiesList
        )

        citiesListView.adapter = arrayAdapter
        citiesListView.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        CommonObject.flag = true
        var chosenCityName:String = parent?.getItemAtPosition(position).toString()

        //CommonObject.cityName = chosenCityName

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("chosenCityName", chosenCityName)
        this.startActivity(intent)
    }
}
package com.example.weatherappbyssm.Common

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import com.example.weatherappbyssm.MainActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices.FusedLocationApi

class LocationWorker : LocationListener {

    var googleApiClient: GoogleApiClient? = null
    var locationRequest: LocationRequest? = null


    fun locationRequest() {
        //Интервал запроса координат = 20 с (обновление)
        locationRequest!!.interval = 20000
        //Интервал запроса координат (продолжительность показа ProgressBar) = 10 с
        locationRequest!!.fastestInterval = 10000
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //Проверка разрешений на определения местополжения
        if (checkSelfPermission(
                MainActivity(),
                ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED && checkSelfPermission(
                MainActivity(),
                ACCESS_COARSE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            return
        }

        FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            this
        )
    }

    override fun onLocationChanged(location: Location?) {
        MainActivity().Presenter().execute(
            CommonObject.apiRequestCurrentWeather(
                location!!.latitude.toString(),
                location.longitude.toString()
            )
        )
    }
}
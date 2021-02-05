package com.example.weatherappbyssm.Common

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import com.example.weatherappbyssm.MainActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.FusedLocationApi

class LocationWorker : LocationListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    var googleApiClient: GoogleApiClient? = null
    var locationRequest: LocationRequest? = null

    //Перенос в Main
    /*private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                MainActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                MainActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            MainActivity().requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), Constants.PERMISSION_REQUEST_CODE
            )
    }

    //Проверка доступа к Google Play Services
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (LocationWorker().isGooglePlayServicesAvailable()) {
                        LocationWorker().buildGoogleApiClient()
                    }
                }
            }
        }
    }*/

    fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability()
        val connectionResult = googleApiAvailability.isGooglePlayServicesAvailable(MainActivity())

        if (connectionResult != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(connectionResult)) {
                googleApiAvailability.getErrorDialog(
                    MainActivity(),
                    connectionResult,
                    Constants.GOOGLE_PLAY_SERVICE_RESOLUTION_REQUEST
                ).show()
            } else {
                Toast.makeText(
                    MainActivity(),
                    "Google Play Services is not supported by this device",
                    Toast.LENGTH_SHORT
                ).show()
                MainActivity().finish()
            }
            return false
        }
        return true
    }

    fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(MainActivity())
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
    }


    override fun onConnected(connectionHint: Bundle?) {
        Log.i("CONNECTION", "Connected to GoogleApiClient")

        locationRequest()
        locationUpdate()
    }

    override fun onConnectionSuspended(cause: Int) {
        Log.i("CONNECTION", "Connection suspended")

        googleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i("ERROR", "Connection is failed, error code: " + connectionResult.errorCode)
    }

    //Запрос на определение местоположения
    private fun locationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.interval = Constants.LONG_INTERVAL
        locationRequest!!.fastestInterval = Constants.SHORT_INTERVAL
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun locationUpdate() {
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

        if (!CommonObject.isCityChosen)
            FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                this
            )
    }

    override fun onLocationChanged(location: Location?) {
        MainActivity().Presenter().execute(
            CommonObject.apiRequestCurrentWeatherByCoordinates(
                location!!.latitude.toString(),
                location.longitude.toString()
            )
        )
    }
}
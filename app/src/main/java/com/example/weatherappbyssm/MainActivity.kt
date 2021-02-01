package com.example.weatherappbyssm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weatherappbyssm.Common.CommonObject
import com.example.weatherappbyssm.Common.Constants
import com.example.weatherappbyssm.Common.Constants.GOOGLE_PLAY_SERVICE_RESOLUTION_REQUEST
import com.example.weatherappbyssm.Common.Constants.LONG_INTERVAL
import com.example.weatherappbyssm.Common.Constants.PERMISSION_REQUEST_CODE
import com.example.weatherappbyssm.Common.Constants.SHORT_INTERVAL
import com.example.weatherappbyssm.Common.OkHttpHelper
import com.example.weatherappbyssm.Model.Root
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.FusedLocationApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.net.URL
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {

    var googleApiClient: GoogleApiClient? = null
    var locationRequest: LocationRequest? = null

    private var openWeatherMap = Root()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestLocationPermissions()

        if (isGooglePlayServicesAvailable()) buildGoogleApiClient()
    }

    //Запрос разрешений на доступ к местоположению устройства
    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), PERMISSION_REQUEST_CODE
            )
    }


    //Проверка доступа к Google Play Services
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isGooglePlayServicesAvailable()) {
                        buildGoogleApiClient()
                    }
                }
            }
        }
    }

    //Проверка поддерживает ли используемое устройство Google Play Services
    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability()
        val connectionResult = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (connectionResult != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(connectionResult)) {
                googleApiAvailability.getErrorDialog(
                    this,
                    connectionResult,
                    GOOGLE_PLAY_SERVICE_RESOLUTION_REQUEST
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Google Play Services not supported by this device",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            return false
        }
        return true
    }

    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
    }


    override fun onConnected(p0: Bundle?) {
        locationRequest()
    }

    override fun onConnectionSuspended(p0: Int) {
        googleApiClient!!.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("ERROR", "Connection is failed, error code: " + p0.errorCode)
    }


    private fun locationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.interval = LONG_INTERVAL
        locationRequest!!.fastestInterval = SHORT_INTERVAL
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //Проверка разрешений на определения местополжения
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
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
        Presenter().execute(
            CommonObject.apiRequestCurrentWeather(
                location!!.latitude.toString(),
                location.longitude.toString()
            )
        )

        /*Presenter().execute(
            CommonObject.apiRequestWeatherForecast(
                location!!.latitude.toString(),
                location.longitude.toString()
            )
        )*/
    }


    override fun onStart() {
        super.onStart()

        if (googleApiClient != null)
            googleApiClient!!.connect()
    }

    override fun onDestroy() {
        googleApiClient!!.disconnect()
        Presenter().cancel()

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        isGooglePlayServicesAvailable()
    }


    inner class Presenter : CoroutineScope {
        private var job: Job = Job()
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job // to run code in Main(UI) Thread

        //Остановка работы Coroutine, когда пользователь звкрывает окно
        fun cancel() {
            job.cancel()
        }

        fun execute(url: String) = launch {
            onPreExecute()
            val result = doInBackground(url) // работает в фоновом потоке, не блокируя основной поток
            onPostExecute(result)
        }

        private suspend fun doInBackground(url: String): String =
            withContext(Dispatchers.IO) { // для заупска кода в фоновом потоке
                val okHttpHelper = OkHttpHelper()

                return@withContext okHttpHelper.makeRequest(url)
            }

        // Runs on the Main(UI) Thread
        private fun onPreExecute() {
            mainContainer.visibility = View.INVISIBLE
            loaderProgressBar.visibility = View.VISIBLE
        }

        // Runs on the Main(UI) Thread
        private fun onPostExecute(result: String) {
            getDataFromJson(result)
            showWeatherDataUI()
            showHideWeatherDetails()
        }
    }

    fun showWeatherDataUI() {
        loaderProgressBar.visibility = View.INVISIBLE
        mainContainer.visibility = View.VISIBLE
        showHideWeatherDetailsButtonsContainer.visibility = View.VISIBLE

        //Отображение данных о погоде
        cityNameTextView.text = "${openWeatherMap.name}, ${openWeatherMap.sys!!.country}"
        cityCoordinatesTextView.text =
            "${openWeatherMap.coord!!.lat}, ${openWeatherMap.coord!!.lon}"
        weatherStatus.text = "${openWeatherMap.weather!![0].description}"
        currentTemperatureTextView.text = "${(openWeatherMap.main!!.temp).toInt()}°C"
        lastWeatherUpdateAtTextView.text = "Last update: " + CommonObject.currentDate
        windTextView.text = "${openWeatherMap.wind!!.speed} m/s"
        pressureTextView.text = "${openWeatherMap.main!!.pressure} hPa"
        humidityTextView.text = "${openWeatherMap.main!!.humidity} %"
        sunriseTextView.text =
            "Sunrise at: " + CommonObject.unixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)
        sunsetTextView.text =
            "Sunset at: " + CommonObject.unixTimeStampToDateTime(openWeatherMap.sys!!.sunset)

        //Показ соответсвующих текущей погоде изображений
        Picasso.with(this@MainActivity)
            .load(CommonObject.getWeatherImage(openWeatherMap.weather!![0].icon!!))
            .into(weatherImageView)
    }

    private fun getDataFromJson(result: String?) {
        val gson = Gson()
        val objectsType = object : TypeToken<Root>() {}.type

        openWeatherMap = gson.fromJson<Root>(result, objectsType)
    }

    private fun showHideWeatherDetails() {
        showWeatherDetailsButton.setOnClickListener {
            detailsContainer.visibility = View.VISIBLE
        }
        hideWeatherDetailsButton.setOnClickListener {
            detailsContainer.visibility = View.INVISIBLE
        }
    }
}
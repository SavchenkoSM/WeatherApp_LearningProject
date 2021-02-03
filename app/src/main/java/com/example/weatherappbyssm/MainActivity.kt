package com.example.weatherappbyssm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
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
import java.io.File
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    var googleApiClient: GoogleApiClient? = null
    var locationRequest: LocationRequest? = null

    private var openWeatherMap = Root()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CommonObject.isCityChosen = false

        requestLocationPermissions()
        if (isGooglePlayServicesAvailable()) buildGoogleApiClient()

        changeCityButton.setOnClickListener(this)
        showWeatherDetailsButton.setOnClickListener(this)
        hideWeatherDetailsButton.setOnClickListener(this)
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


    override fun onConnected(connectionHint: Bundle?) {
        Log.i("CONNECTION", "Connected to GoogleApiClient")

        locationRequest()
    }

    override fun onConnectionSuspended(cause: Int) {
        Log.i("CONNECTION", "Connection suspended")

        googleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i("ERROR", "Connection is failed, error code: " + connectionResult.errorCode)
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
        if (CommonObject.isCityChosen) {
            Presenter().execute(
                CommonObject.apiRequestCurrentWeatherByCityName(CommonObject.cityName.toString())
            )
        } else {
            Presenter().execute(
                CommonObject.apiRequestCurrentWeatherByCoordinates(
                    location!!.latitude.toString(),
                    location.longitude.toString()
                )
            )
        }
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
            get() = Dispatchers.Main + job // для выполнения в основном потоке

        //Остановка работы Coroutine, когда пользователь закрывает окно
        fun cancel() {
            job.cancel()
        }

        fun execute(url: String) = launch {
            onPreExecute()
            // работает в фоновом потоке, не блокируя основной поток
            val result = doInBackground(url)
            onPostExecute(result)
        }

        // для заупска кода в фоновом потоке
        private suspend fun doInBackground(url: String): String = withContext(Dispatchers.IO) {
            val okHttpHelper = OkHttpHelper()

            return@withContext okHttpHelper.makeRequest(url)
        }

        // Выполнение в основном потоке
        private fun onPreExecute() {
            mainContainer.visibility = View.INVISIBLE
            loaderProgressBar.visibility = View.VISIBLE
        }

        // Выполнение в основном потоке
        private fun onPostExecute(result: String) {
            getDataFromJson(result)
            rememberNewCity()
            showWeatherDataUI()
        }
    }

    private fun rememberNewCity() {
        if (!CommonObject.isCityChosen)
            CommonObject.cityName = openWeatherMap.name
    }

    private fun getDataFromJson(result: String?) {
        val gson = Gson()
        val objectsType = object : TypeToken<Root>() {}.type

        openWeatherMap = gson.fromJson<Root>(result, objectsType)
    }

    private fun showWeatherDataUI() {
        loaderProgressBar.visibility = View.INVISIBLE
        mainContainer.visibility = View.VISIBLE

        //Отображение данных о погоде
        cityNameTextView.text = "${openWeatherMap.name}, ${openWeatherMap.sys!!.country}"
        cityCoordinatesTextView.text =
            "${openWeatherMap.coord!!.lat}, ${openWeatherMap.coord!!.lon}"
        weatherStatus.text = "${openWeatherMap.weather!![0].description}"
        currentTemperatureTextView.text = "${(openWeatherMap.main!!.temp).toInt()}°C"
        tempFeelsLikeTextView.text = "Feels like: ${(openWeatherMap.main!!.feels_like).toInt()}°C"
        lastWeatherUpdateAtTextView.text = "Last update: " + CommonObject.currentDate
        windTextView.text = "${openWeatherMap.wind!!.speed} m/s"
        pressureTextView.text = "${openWeatherMap.main!!.pressure} hPa"
        humidityTextView.text = "${openWeatherMap.main!!.humidity} %"
        minTempTextView.text = "Min temp: ${openWeatherMap.main!!.temp_min}°C"
        maxTempTextView.text = "Max temp: ${openWeatherMap.main!!.temp_max}°C"
        sunriseTextView.text =
            "Sunrise at: " + CommonObject.unixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)
        sunsetTextView.text =
            "Sunset at: " + CommonObject.unixTimeStampToDateTime(openWeatherMap.sys!!.sunset)

        //Показ соответсвующих текущей погоде изображений
        Picasso.with(this@MainActivity)
            .load(CommonObject.getWeatherImage(openWeatherMap.weather!![0].icon!!))
            .into(weatherImageView)
    }

    override fun onClick(view: View?) {
        when (view) {
            changeCityButton -> {
                val intent = Intent(this, AddedCitiesActivity::class.java)
                startActivity(intent)
            }
            showWeatherDetailsButton -> detailsContainer.visibility = View.VISIBLE
            hideWeatherDetailsButton -> detailsContainer.visibility = View.INVISIBLE
        }
    }
}
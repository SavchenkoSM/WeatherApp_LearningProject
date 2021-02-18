package com.example.weatherappbyssm.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weatherappbyssm.R
import com.example.weatherappbyssm.common.*
import com.example.weatherappbyssm.common.Constants.GOOGLE_PLAY_SERVICE_RESOLUTION_REQUEST
import com.example.weatherappbyssm.common.Constants.PERMISSION_REQUEST_CODE
import com.example.weatherappbyssm.models.Root
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
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Класс основной активности, отображающей погоду для текущего/выбранного города
 */
class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private var locationClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationManager: LocationManager? = null

    private var isGpsEnabled: Boolean = false
    private var isLocationPermissionsGranted: Boolean = false

    private var openWeatherMap = Root()
    private var dbHelper: DBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        if (!CommonObject.isCityChosen)
            requestLocationPermissions()

        if (isLocationPermissionsGranted && isGooglePlayServicesAvailable()) {
            if (!CommonObject.isCityChosen)
                checkLocationServicesEnabled()
            buildGoogleApiClient()
        }

        if (CommonObject.isCityChosen)
            WeatherPresenter().execute(
                CommonObject.apiRequestCurrentWeatherByCityName(CommonObject.chosenCityName.toString())
            )

        updateDataImageView.setOnClickListener(this)
        changeCityTextView.setOnClickListener(this)
    }


    /**
     * Запрос разрешений на доступ к местоположению устройства
     */
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
                ),
                PERMISSION_REQUEST_CODE
            )
        else
            isLocationPermissionsGranted = true
    }

    /**
     * Проверка, даны ли необходимые разрешения и последующий запуск получения местоположения
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {

        when (requestCode) {
            PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(
                        this,
                        "Permissions granted",
                        Toast.LENGTH_SHORT).show()

                    if (isGooglePlayServicesAvailable()) {
                        if (!CommonObject.isCityChosen)
                            checkLocationServicesEnabled()
                        buildGoogleApiClient()
                    }

                } else
                    showCacheDataIfExist()
        }
    }

    /**
     * Вывод закешированных данных, если таковые имеются
     */
    private fun showCacheDataIfExist() {
        if (!WorkWithCacheTableFromDB(this).isCacheTableHasEmptyRow())
            WorkWithCacheTableFromDB(this).getCacheDataFromDB()

        if (!WeatherDataForDisplay.cityName.isNullOrEmpty())
            showWeatherDataUI()
        else {
            errorTextView.text = getString(R.string.exception)
            errorTextView.visibility = View.VISIBLE
        }
    }

    /**
     * Проверка включен ли GPS
     */
    private fun checkLocationServicesEnabled(): Boolean {
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            isGpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (gpsException: Exception) {
            Toast.makeText(
                this,
                "Something went wrong. Check your GPS enabling",
                Toast.LENGTH_LONG
            ).show()
        }
        return buildAlertDialogLocationServicesDisabled(isGpsEnabled)
    }

    /**
     * Вывод диалогового окна с последующим переводом пользователя в настройки для включения GPS
     */
    private fun buildAlertDialogLocationServicesDisabled(isGpsEnabled: Boolean): Boolean {
        if (!isGpsEnabled) {
            val alertDialogBuilder = AlertDialog.Builder(
                this,
                R.style.AlertDialog
            )
            alertDialogBuilder.setMessage("GPS is disabled. Please, enable it for the app work")
                .setTitle("GPS disabled")
                .setCancelable(false)
                .setPositiveButton("Go to Settings") { dialog, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    dialog.cancel()
                }
            val alert: AlertDialog = alertDialogBuilder.create()
            alert.show()
            return true
        }
        return false
    }


    /**
     * Проверка, поддерживает ли используемое устройство Google Play Services
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability()
        val connectionResult = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (connectionResult != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(connectionResult))
                googleApiAvailability.getErrorDialog(
                    this,
                    connectionResult,
                    GOOGLE_PLAY_SERVICE_RESOLUTION_REQUEST
                ).show()
            else {
                Toast.makeText(
                    this,
                    "Google Play Services is not supported by this device",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            return false
        }
        return true
    }

    /**
     * Построение Api Client для определения местоположения и подключение
     */
    private fun buildGoogleApiClient() {
        locationClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        locationClient?.connect()
    }


    /**
     * Обработка при установленном подключении
     */
    override fun onConnected(connectionHint: Bundle?) {
        Log.i("CONNECTION", "Connected to GoogleApiClient successfully")

        if (!CommonObject.isCityChosen)
            locationRequest()
    }

    /**
     * Обработка приостановления подключения
     */
    override fun onConnectionSuspended(cause: Int) {
        Log.i("CONNECTION", "Connection suspended")

        locationClient?.connect()
    }

    /**
     * Обработка ошибки подключения
     */
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i("ERROR", "Connection is failed, error code: " + connectionResult.errorCode)
    }


    /**
     * Запрос на определение текущего местоположения
     */
    private fun locationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //Проверка разрешений на определения местополжения
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            return

        FusedLocationApi.requestLocationUpdates(
            locationClient,
            locationRequest,
            this
        )
    }

    /**
     * Обработка изменения текущего местоположения
     */
    override fun onLocationChanged(location: Location?) {
        WeatherPresenter().execute(
            CommonObject.apiRequestCurrentWeatherByCoordinates(
                location?.latitude,
                location?.longitude
            )
        )
    }


    /**
     * Обработка жизненного цикла активности
     * Возобновление работы приостановленного приложения
     */
    override fun onStart() {
        super.onStart()

        locationClient?.connect()
    }

    /**
     * Прекращение всех процессов и разрушение активности по окончании работы
     */
    override fun onDestroy() {
        locationClient?.disconnect()
        WeatherPresenter().cancel()

        super.onDestroy()
    }

    /**
     * Вызывается, когда активность выполняется на переднем плане,
     * инициализирует приостановленные компоненты
     */
    override fun onResume() {
        super.onResume()

        isGooglePlayServicesAvailable()
    }


    /**
     * Coroutine для работы с отображением погоды
     */
    inner class WeatherPresenter : CoroutineScope {
        private var job: Job = Job()
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job // Для выполнения в основном потоке

        // Запись данных о погоде для последнего отображенного города, если таковые имеются
        // и остановка работы Coroutine при закрытии окна
        fun cancel() {
            if (WeatherDataForDisplay.cityName != null)
                WorkWithCacheTableFromDB(this@MainActivity).updateCacheDataInDB(
                    WeatherDataForDisplay.cityName,
                    WeatherDataForDisplay.country,
                    WeatherDataForDisplay.latitude,
                    WeatherDataForDisplay.longitude,
                    WeatherDataForDisplay.skyStatus,
                    WeatherDataForDisplay.currentTemp,
                    WeatherDataForDisplay.tempFeelsLike,
                    WeatherDataForDisplay.lastWeatherUpdateTime,
                    WeatherDataForDisplay.windSpeed,
                    WeatherDataForDisplay.pressure,
                    WeatherDataForDisplay.humidity,
                    WeatherDataForDisplay.minTemp,
                    WeatherDataForDisplay.maxTemp,
                    WeatherDataForDisplay.sunriseTime,
                    WeatherDataForDisplay.sunsetTime
                )
            job.cancel()
        }

        fun execute(url: String) = launch {
            onPreExecute()
            // Работает в фоновом потоке, не блокируя основной поток
            val result = doInBackground(url)
            onPostExecute(result)
        }

        // Для запуска в фоновом потоке
        private suspend fun doInBackground(url: String): String = withContext(Dispatchers.IO) {
            val okHttpHelper = OkHttpHelper()
            val response: String

            response = try {
                okHttpHelper.makeRequest(url)
            } catch (exception: IOException) {
                exception.printStackTrace()
                null.toString()
            }
            return@withContext response
        }

        // Выполнение в основном потоке
        private fun onPreExecute() {
            mainContainer.visibility = View.GONE
            errorTextView.visibility = View.GONE
            loaderProgressBar.visibility = View.VISIBLE
        }

        // Выполнение в основном потоке
        private fun onPostExecute(result: String) {
            try {
                getWeatherDataFromJson(result)
                rememberNewCity()
                openWeatherMapDataForDisplay()
                showWeatherDataUI()
                showWeatherImage()
            } catch (exception: Exception) {
                loaderProgressBar.visibility = View.GONE
                if (WeatherDataForDisplay.cityName != null) {
                    errorTextView.text = getString(R.string.exception)
                    errorTextView.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Получение данных о погоде из Json
     */
    private fun getWeatherDataFromJson(result: String?) {
        val gson = Gson()
        val objectsType = object : TypeToken<Root>() {}.type

        openWeatherMap = gson.fromJson<Root>(result, objectsType)
    }

    /**
     * Запоминание нового города при измененении местоположения
     */
    private fun rememberNewCity() {
        if (!CommonObject.isCityChosen) {
            CommonObject.newCityName = openWeatherMap.name
            updateDataImageView.visibility = View.INVISIBLE
        }
    }

    /**
     * Запись набора данных с сервера API OpenWeatherMap для отображения
     */
    private fun openWeatherMapDataForDisplay() {
        WeatherDataForDisplay.cityName = openWeatherMap.name
        WeatherDataForDisplay.country = openWeatherMap.sys!!.country
        WeatherDataForDisplay.latitude = openWeatherMap.coord!!.lat
        WeatherDataForDisplay.longitude = openWeatherMap.coord!!.lon
        WeatherDataForDisplay.skyStatus = openWeatherMap.weather!![0].description
        WeatherDataForDisplay.currentTemp = openWeatherMap.main!!.temp
        WeatherDataForDisplay.tempFeelsLike = openWeatherMap.main!!.feels_like
        WeatherDataForDisplay.lastWeatherUpdateTime = CommonObject.currentDate
        WeatherDataForDisplay.windSpeed = openWeatherMap.wind!!.speed
        WeatherDataForDisplay.pressure = openWeatherMap.main!!.pressure
        WeatherDataForDisplay.humidity = openWeatherMap.main!!.humidity
        WeatherDataForDisplay.minTemp = openWeatherMap.main!!.temp_min
        WeatherDataForDisplay.maxTemp = openWeatherMap.main!!.temp_max
        WeatherDataForDisplay.sunriseTime =
            CommonObject.unixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)
        WeatherDataForDisplay.sunsetTime =
            CommonObject.unixTimeStampToDateTime(openWeatherMap.sys!!.sunset)
    }

    /**
     * Отображение данных о погоде
     */
    private fun showWeatherDataUI() {
        cityNameTextView.text =
            "${WeatherDataForDisplay.cityName}, ${WeatherDataForDisplay.country}"
        cityCoordinatesTextView.text =
            "${WeatherDataForDisplay.latitude}, ${WeatherDataForDisplay.longitude}"
        weatherStatusTextView.text = WeatherDataForDisplay.skyStatus
        currentTemperatureTextView.text = "${WeatherDataForDisplay.currentTemp.toInt()}°C"
        tempFeelsLikeTextView.text = "Feels like: ${WeatherDataForDisplay.tempFeelsLike.toInt()}°C"
        lastWeatherUpdateAtTextView.text =
            "Updated at: ${WeatherDataForDisplay.lastWeatherUpdateTime}"
        windTextView.text = "${WeatherDataForDisplay.windSpeed} m/s"
        pressureTextView.text = "${WeatherDataForDisplay.pressure} hPa"
        humidityTextView.text = "${WeatherDataForDisplay.humidity} %"
        minTempTextView.text = "Min temp: ${WeatherDataForDisplay.minTemp}°C"
        maxTempTextView.text = "Max temp: ${WeatherDataForDisplay.maxTemp}°C"
        sunriseTextView.text = "Sunrise at: ${WeatherDataForDisplay.sunriseTime}"
        sunsetTextView.text = "Sunset at: ${WeatherDataForDisplay.sunsetTime}"

        loaderProgressBar.visibility = View.GONE
        mainContainer.visibility = View.VISIBLE
    }

    /**
     * Отображение картинок, соответствующих текущей погоде
     */
    fun showWeatherImage() {
        Picasso.with(this@MainActivity)
            .load(CommonObject.getWeatherImage(openWeatherMap.weather!![0].icon!!))
            .into(weatherImageView)
    }

    /**
     * Обработка нажатий на компоненты взаимодействия
     */
    override fun onClick(view: View?) {
        when (view) {
            updateDataImageView ->
                if (CommonObject.isCityChosen)
                    WeatherPresenter().execute(
                        CommonObject.apiRequestCurrentWeatherByCityName(
                            CommonObject.chosenCityName.toString()
                        )
                    )
            changeCityTextView -> {
                val mainActivityIntent = Intent(this, AddedCitiesActivity::class.java)
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(mainActivityIntent)
            }
        }
    }
}
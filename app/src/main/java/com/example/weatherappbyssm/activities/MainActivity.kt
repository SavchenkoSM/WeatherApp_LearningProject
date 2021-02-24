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
import com.bumptech.glide.Glide
import com.example.weatherappbyssm.R
import com.example.weatherappbyssm.common.CommonObject
import com.example.weatherappbyssm.common.ConstantsObject.GOOGLE_PLAY_SERVICE_RESOLUTION_REQUEST
import com.example.weatherappbyssm.common.ConstantsObject.PERMISSION_REQUEST_CODE
import com.example.weatherappbyssm.common.ConstantsObject.REQUEST_GPS_CODE
import com.example.weatherappbyssm.common.WeatherDataForDisplayObject
import com.example.weatherappbyssm.database.DBHelper
import com.example.weatherappbyssm.database.WorkWithCacheTableFromDB
import com.example.weatherappbyssm.database.WorkWithCitiesTableFromDB
import com.example.weatherappbyssm.http.NetworkConnection
import com.example.weatherappbyssm.http.OkHttpHelper
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/**
 * Класс основной активности, отображающей погоду для текущего/выбранного города
 */
class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    // Переменные для работы с определением текущего местоположения
    private var locationClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationManager: LocationManager? = null

    private var isGpsEnabled: Boolean = false
    private var isLocationPermissionsGranted: Boolean = false

    private var latitude: Double? = null
    private var longitude: Double? = null

    // Инициализация модели для работы с данными о погоде
    private var openWeatherMap = Root()
    // Для работы с БД
    private var dbHelper: DBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        /**
         * Если город выбран - вызов корутины для получения данных о погоде для него,
         * иначе вызов методов для получения данных о погоде по текущим координатам
         */
        if (CommonObject.isCityChosen)
            WeatherPresenter().execute()
        else
            startWorkWithWeatherByCoordinates()

        updateDataImageView.setOnClickListener(this)
        changeCityTextView.setOnClickListener(this)

        Glide.with(this)
            .load(R.drawable.loading_animation)
            .into(loadingIcon)
    }

    /**
     * Методы для работы с определением текущего местоположения
     */

    /**
     * Работа над получением данных о погоде по текущим координатам
     */
    private fun startWorkWithWeatherByCoordinates() {
        requestLocationPermissions()
        if (isLocationPermissionsGranted && isGooglePlayServicesAvailable()) {
            isLocationServicesDisabled()
            buildGoogleApiClient()
        }
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
        else {
            Toast.makeText(
                this,
                "Location permissions granted",
                Toast.LENGTH_SHORT
            ).show()

            isLocationPermissionsGranted = true
        }
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
                        "Location permissions granted",
                        Toast.LENGTH_SHORT).show()

                    if (isGooglePlayServicesAvailable()) {
                        isLocationServicesDisabled()
                        buildGoogleApiClient()
                    }

                } else {
                    Toast.makeText(
                        this,
                        "Location permissions denied",
                        Toast.LENGTH_SHORT).show()
                    errorTextView.text = getString(R.string.location_permission_denied)

                    showCacheDataIfExist()
                }
        }
    }


    /**
     * Проверка включен ли GPS
     */
    private fun isLocationServicesDisabled(): Boolean {
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
            alertDialogBuilder.setMessage(getString(R.string.gps_disabled))
                .setTitle("GPS disabled")
                .setCancelable(false)
                .setNegativeButton("Cancel") { dialog, _ ->
                    errorTextView.text = getString(R.string.gps_disabled)
                    showCacheDataIfExist()
                    dialog.cancel()
                }
                .setPositiveButton("Go to Settings") { dialog, _ ->
                    startActivityForResult(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        REQUEST_GPS_CODE
                    )
                    showLoadingView()
                    dialog.cancel()
                }
            val alert: AlertDialog = alertDialogBuilder.create()
            alert.show()
            return true
        }
        else
            showLoadingView()
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
     * Построение API Client для определения местоположения и подключение
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
     * Обработка при установленном подключении к API
     */
    override fun onConnected(connectionHint: Bundle?) {
        Log.i("CONNECTION", "Connected to GoogleApiClient successfully")

        if (!CommonObject.isCityChosen)
            locationRequest()
    }

    /**
     * Обработка приостановления подключения к API
     */
    override fun onConnectionSuspended(cause: Int) {
        Log.i("CONNECTION", "Connection suspended")

        locationClient?.connect()
    }

    /**
     * Обработка ошибки подключения к API
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

        // Проверка разрешений на определения местополжения
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
        latitude = location?.latitude
        longitude = location?.longitude

        WeatherPresenter().execute()
    }


    /**
     * Методы, обрабатывающие жизненный цикл активности
     */

    /**
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
     * Вызывается, когда происходит свертывание активности.
     * Сохраняет незафиксированные данные.
     * Запись данных о погоде для последнего отображенного города, если таковые имеются
     */
    override fun onPause() {
        super.onPause()

        if (WeatherDataForDisplayObject.cityName != null)
            WorkWithCacheTableFromDB(this).updateCacheDataInDB(
                WeatherDataForDisplayObject.cityName,
                WeatherDataForDisplayObject.country,
                WeatherDataForDisplayObject.latitude,
                WeatherDataForDisplayObject.longitude,
                WeatherDataForDisplayObject.skyStatus,
                WeatherDataForDisplayObject.currentTemp,
                WeatherDataForDisplayObject.tempFeelsLike,
                WeatherDataForDisplayObject.lastWeatherUpdateTime,
                WeatherDataForDisplayObject.windSpeed,
                WeatherDataForDisplayObject.pressure,
                WeatherDataForDisplayObject.humidity,
                WeatherDataForDisplayObject.minTemp,
                WeatherDataForDisplayObject.maxTemp,
                WeatherDataForDisplayObject.sunriseTime,
                WeatherDataForDisplayObject.sunsetTime
            )
    }


    /**
     * Корутина для работы с отображением погоды
     */
    inner class WeatherPresenter : CoroutineScope {
        private var job: Job = Job()
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job // Для выполнения в основном потоке

        /**
         * Остановка работы корутины при закрытии окна
         */
        fun cancel() {
            job.cancel()
        }

        fun execute() = launch {
            onPreExecute()
            // Работает в фоновом потоке, не блокируя основной поток
            val result = doInBackground()
            onPostExecute(result)
        }

        // Для запуска в фоновом потоке
        private suspend fun doInBackground(): String = withContext(Dispatchers.IO) {
            val okHttpHelper = OkHttpHelper()
            val response: String
            // Определение метода получения данных о погоде.
            // Если город не выбран - запрос к API по координатам,
            // иначе - по названию города
            val url = if (!CommonObject.isCityChosen)
                CommonObject.apiRequestCurrentWeatherByCoordinates(latitude, longitude)
            else
                CommonObject.apiRequestCurrentWeatherByCityName(CommonObject.chosenCityName.toString())

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
            loadingContainer.visibility = View.GONE
            mainContainer.visibility = View.GONE
            errorTextView.visibility = View.GONE
            loaderProgressBar.visibility = View.VISIBLE
        }

        // Выполнение в основном потоке
        private fun onPostExecute(result: String) {
            try {
                getWeatherDataFromJson(result)
                rememberNewCity()
                writeOpenWeatherMapDataForDisplay()
                showWeatherDataUI()
                showWeatherImage()
            } catch (exception: Exception) {
                loaderProgressBar.visibility = View.GONE

                /**
                 * Исключение возникает, когда данные для отображения не получены.
                 * Возможные причины:
                 * 1. Отсутсвие/нестабильность Интернет-соединения
                 * 2. Не дано разрешение на определение местоположения
                 * 3. Разрешение на определение местоположения дано, но не включен GPS
                 */
                if (!NetworkConnection(this@MainActivity).isOnline())
                    buildAlertDialogNoInternet()
                else
                    startWorkWithWeatherByCoordinates()
            }
        }
    }


    /**
     * Вывод диалогового окна при отсутвии Интернет-соединения
     * с возможностью повторного запуска корутины
     */
    private fun buildAlertDialogNoInternet() {
        val alertDialogBuilder = AlertDialog.Builder(
            this@MainActivity,
            R.style.AlertDialog
        )
        alertDialogBuilder.setMessage("Please, check your Internet connection and try again")
            .setTitle("Unstable Internet")
            .setCancelable(false)
            .setNegativeButton("No") { dialog, _ ->
                showCacheDataIfExist()
                dialog.cancel()
            }
            .setPositiveButton("Try again") { dialog, _ ->
                WeatherPresenter().execute()
                dialog.cancel()
            }
        val alert: AlertDialog = alertDialogBuilder.create()
        alert.show()
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

            // Добавление нового города в БД, если он там отсутсвует
            // и если переменная города не пустая или не равна null
            if (!CommonObject.newCityName.isNullOrEmpty())
                WorkWithCitiesTableFromDB(this)
                    .addNewCityToDB(CommonObject.newCityName.toString())
        }
    }

    /**
     * Запись набора данных с сервера API OpenWeatherMap в переменные для отображения
     */
    private fun writeOpenWeatherMapDataForDisplay() {
        WeatherDataForDisplayObject.cityName = openWeatherMap.name
        WeatherDataForDisplayObject.country = openWeatherMap.sys!!.country
        WeatherDataForDisplayObject.latitude = openWeatherMap.coord!!.lat
        WeatherDataForDisplayObject.longitude = openWeatherMap.coord!!.lon
        WeatherDataForDisplayObject.skyStatus = openWeatherMap.weather!![0].description
        WeatherDataForDisplayObject.currentTemp = openWeatherMap.main!!.temp
        WeatherDataForDisplayObject.tempFeelsLike = openWeatherMap.main!!.feels_like
        WeatherDataForDisplayObject.lastWeatherUpdateTime = CommonObject.currentDate
        WeatherDataForDisplayObject.windSpeed = openWeatherMap.wind!!.speed
        WeatherDataForDisplayObject.pressure = openWeatherMap.main!!.pressure
        WeatherDataForDisplayObject.humidity = openWeatherMap.main!!.humidity
        WeatherDataForDisplayObject.minTemp = openWeatherMap.main!!.temp_min
        WeatherDataForDisplayObject.maxTemp = openWeatherMap.main!!.temp_max
        WeatherDataForDisplayObject.sunriseTime =
            CommonObject.unixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)
        WeatherDataForDisplayObject.sunsetTime =
            CommonObject.unixTimeStampToDateTime(openWeatherMap.sys!!.sunset)
    }


    /**
     * Методы отображения
     */

    /**
     * Отображение закешированных данных, если таковые имеются
     * Если данных нет, вывод соответсвующего сообщения пользователю
     */
    private fun showCacheDataIfExist() {
        if (!WorkWithCacheTableFromDB(this).isCacheTableHasEmptyRow())
            WorkWithCacheTableFromDB(this).getCacheDataFromDB()

        if (!WeatherDataForDisplayObject.cityName.isNullOrEmpty())
            showWeatherDataUI()
        else
            errorTextView.visibility = View.VISIBLE
    }

    /**
     * Отображение анимации загрузки на время, пока координаты текущего города не получены
     */
    private fun showLoadingView() {
        if (latitude == null || longitude == null)
            loadingContainer.visibility = View.VISIBLE
    }

    /**
     * Отображение данных о погоде
     */
    private fun showWeatherDataUI() {
        cityNameTextView.text =
            "${WeatherDataForDisplayObject.cityName}, ${WeatherDataForDisplayObject.country}"
        cityCoordinatesTextView.text =
            "${WeatherDataForDisplayObject.latitude}, ${WeatherDataForDisplayObject.longitude}"
        weatherStatusTextView.text = WeatherDataForDisplayObject.skyStatus
        currentTemperatureTextView.text = "${WeatherDataForDisplayObject.currentTemp.toInt()}°C"
        tempFeelsLikeTextView.text =
            "Feels like: ${WeatherDataForDisplayObject.tempFeelsLike.toInt()}°C"
        lastWeatherUpdateAtTextView.text =
            "Updated at: ${WeatherDataForDisplayObject.lastWeatherUpdateTime}"
        windTextView.text = "${WeatherDataForDisplayObject.windSpeed} m/s"
        pressureTextView.text = "${WeatherDataForDisplayObject.pressure} hPa"
        humidityTextView.text = "${WeatherDataForDisplayObject.humidity} %"
        minTempTextView.text = "Min temp: ${WeatherDataForDisplayObject.minTemp}°C"
        maxTempTextView.text = "Max temp: ${WeatherDataForDisplayObject.maxTemp}°C"
        sunriseTextView.text = "Sunrise at: ${WeatherDataForDisplayObject.sunriseTime}"
        sunsetTextView.text = "Sunset at: ${WeatherDataForDisplayObject.sunsetTime}"

        loaderProgressBar.visibility = View.GONE
        mainContainer.visibility = View.VISIBLE
    }

    /**
     * Отображение изображения, соответствующего текущей облачности
     */
    private fun showWeatherImage() {
        Glide.with(this@MainActivity)
            .load(CommonObject.getWeatherImage(openWeatherMap.weather!![0].icon!!))
            .into(weatherImageView)
    }


    /**
     * Обработка нажатий на компоненты взаимодействия
     */
    override fun onClick(view: View?) {
        when (view) {
            // Вызов корутины для обновления данных о погоде
            updateDataImageView ->
                WeatherPresenter().execute()
            // Переход на активность для выбора города
            changeCityTextView -> {
                val mainActivityIntent = Intent(this, ChooseCityActivity::class.java)
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(mainActivityIntent)
            }
        }
    }
}
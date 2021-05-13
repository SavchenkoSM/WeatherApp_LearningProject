# WeatherApp [Learning project]
### :sun_behind_rain_cloud: Weather App
Client-server app for displaying weather of the current city and cities from the list.
* When the application is launched, it receives the current geolocation and determine the corresponding city name, add it to the list of cities and display the weather for it on the screen.
* **Functionality**:
   * displaying the weather and its additional characteristics;
   * definition of the current city and adding it to the general list;
   * selecting a city to display the weather.
* In network interaction, the server response delay is accompanied by the display of the spinner. 
* The app provides checks for the lack of the Internet and displays the weather even in the absence of an Internet connection, using cached data, if any.
* The app provides requests for geolocation and use-GPS permissions, in the absence of which, the application displays special alerts for the user.
* **API:** OpenWeatherMap

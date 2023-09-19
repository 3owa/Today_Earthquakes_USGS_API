package com.usgs


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.ktor.client.HttpClient
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class MainViewModel : ViewModel() {

    private val currentDate = getCurrentDate()
    private val apiUrl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=$currentDate"

    // Use MutableState to store the earthquakes data
    private val _earthquakes = MutableStateFlow<List<Earthquake>>(emptyList())
    val earthquakes: StateFlow<List<Earthquake>> = _earthquakes

    init {
        fetchJsonData()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    private fun fetchJsonData() {
        // Create a Ktor HTTP client
        val client = HttpClient(){
            install(Logging) {
                level = LogLevel.NONE
            }
        }

        viewModelScope.launch {
            try {
                // Make a GET request to the API URL
                val response: String = client.get(apiUrl)
                // Update the _jsonData MutableStateFlow with the response
                withContext(Dispatchers.Main) {
                    _earthquakes.value = parseEarthquakeData(response)
                }
            } catch (e: Exception) {
                // Handle any errors here
                e.printStackTrace()
            } finally {
                // Close the HTTP client when done
                client.close()
            }
        }
    }

    private fun parseEarthquakeData(jsonData: String): List<Earthquake> {
        val earthquakeList = mutableListOf<Earthquake>()

        try {
            val jsonObject = JSONObject(jsonData)
            val featuresArray = jsonObject.getJSONArray("features")

            for (i in 0 until featuresArray.length()) {
                val featureObject = featuresArray.getJSONObject(i)
                val propertiesObject = featureObject.getJSONObject("properties")
                val geometryObject = featureObject.getJSONObject("geometry")
                val coordinatesArray = geometryObject.getJSONArray("coordinates")

                val magnitude = propertiesObject.getDouble("mag")
                val place = propertiesObject.getString("place")
                val time = propertiesObject.getLong("time")
                val coordinates = mutableListOf<Double>()
                for (j in 0 until coordinatesArray.length()) {
                    coordinates.add(coordinatesArray.getDouble(j))
                }

                val earthquake = Earthquake(magnitude, place, time, coordinates)
                earthquakeList.add(earthquake)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return earthquakeList
    }
}


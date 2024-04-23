package com.example.quickquery.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickquery.model.response.Country
import com.example.quickquery.repository.DataRepository
import com.example.quickquery.repository.DataSourceType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import android.util.Log
import java.net.SocketTimeoutException

/**
 * ViewModel responsible for fetching country details and managing UI-related data.
 *
 * @property repository The repository used to get country details.
 */
class MainViewModel(private val repository: DataRepository) : ViewModel() {
    val countryData = MutableLiveData<List<Country>?>() // Live data to hold country data.
    val errorMessage = MutableLiveData<String>() // Live data to hold error messages.
    val dataSourceType =
        MutableLiveData<DataSourceType>() // Live data to hold the data source type.
    val dataLoading = MutableLiveData<Boolean>(false)
    private val gson = Gson() // Gson instance for JSON deserialization.

    /**
     * Fetches details for a given country by its name and updates live data accordingly.
     *
     * @param countryName The name of the country to fetch details for.
     */
    fun fetchCountryDetails(countryName: String) {
        viewModelScope.launch {
            try {
                val result = repository.getData(countryName) { isNetworkFetch ->
                    // Update the loading state when fetching data from the network.
                    dataLoading.postValue(isNetworkFetch)
                } // Get data from repository.
                val listType = object :
                    TypeToken<List<Country>>() {}.type // Type token for deserializing JSON to List<Country>.
                val response: List<Country>? =
                    gson.fromJson(result.first, listType) // Deserialize JSON to List<Country>.

                countryData.postValue(response) // Post the fetched data to LiveData.
                dataSourceType.postValue(result.second) // Post the source type of the data (CACHE, DATABASE, NETWORK).

                if (response.isNullOrEmpty()) {
                    // Handle case where no data was fetched.
                    Log.e("MainViewModel", "Error fetching data: No data available for the query")
                    errorMessage.postValue("No data available for the query: $countryName")
                }
            } catch (e: SocketTimeoutException) {
                Log.e("MainViewModel", "Error fetching data", e)
                errorMessage.postValue("Read time out. Please try again.")
            } catch (e: Exception) {
                // Handle unexpected exceptions.
                Log.e("MainViewModel", "Error fetching data", e)
                errorMessage.postValue(e.message ?: "An unknown error occurred")

            }
        }
    }
}

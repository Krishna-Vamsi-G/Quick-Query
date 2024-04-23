package com.example.quickquery.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import com.example.quickquery.model.response.ApiResponse
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface ApiInterface {

    /**
     * Makes a GET request to the REST Countries API to fetch details for a specific country.
     *
     * @param countryName The name of the country for which details are requested.
     * @return A [Response] object containing the [ApiResponse] which includes detailed information about the country.
     */
    @GET("v3.1/name/{name}")
    suspend fun getApiResponse(@Path("name") countryName: String): Response<ApiResponse>

    companion object {
        // Base URL for the REST Countries API.
        private const val BASE_URL = "https://restcountries.com/"

        /**
         * Creates and configures a Retrofit instance for making API calls.
         *
         * @return An instance of [ApiInterface] configured with a Gson converter and the base URL.
         */
        fun create(): ApiInterface {
            val okHttpClient = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS) // Set read timeout
                .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
                .build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build()
            return retrofit.create(ApiInterface::class.java)
        }
    }
}

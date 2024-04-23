package com.example.quickquery.repository

import android.util.Log
import com.example.quickquery.api.ApiInterface
import com.example.quickquery.model.local.CacheDao
import com.example.quickquery.model.local.CacheEntity
import com.example.quickquery.utils.InMemoryCache
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DataRepository(
    private val apiInterface: ApiInterface,
    private val cacheDao: CacheDao
) {
    private val gson = Gson()

    /**
     * Retrieves data based on the provided query string. It first checks the in-memory cache,
     * then the local database, and finally the network if the data is not found or is stale.
     *
     * @param query The query string used to fetch the data.
     * @param onFetchFromNetwork Callback to be invoked when data is about to be fetched from network.
     * @return A Pair containing the data as a JSON string and the data source type.
     */
    suspend fun getData(query: String, onFetchFromNetwork: (Boolean) -> Unit): Pair<String, DataSourceType> {
        val cacheKey = generateCacheKey(query)

        InMemoryCache.load(cacheKey, TimeUnit.MINUTES.toMillis(10))?.let {
            Log.d("DataRepository", "Data loaded from cache.")
            return Pair(it, DataSourceType.CACHE)
        }

        cacheDao.getCachedResponseIfValid(query, TimeUnit.MINUTES.toMillis(10))?.let {
            InMemoryCache.save(cacheKey, it.response)
            Log.d("DataRepository", "Data loaded from database.")
            return Pair(it.response, DataSourceType.DATABASE)
        }

        onFetchFromNetwork(true)

        Log.d("DataRepository", "Fetching data from network.")
        return fetchDataFromNetwork(query, cacheKey)
    }

    private fun generateCacheKey(query: String): String = query.hashCode().toString()

    private suspend fun fetchDataFromNetwork(query: String, cacheKey: String): Pair<String, DataSourceType> {
        val response = makeNetworkRequest(query)
        if (response.isSuccessful) {
            response.body()?.let { apiResponse ->
                val responseDataJson = gson.toJson(apiResponse)
                cacheResponse(query, responseDataJson, System.currentTimeMillis(), cacheKey)
                return Pair(responseDataJson, DataSourceType.NETWORK)
            } ?: throw NoSuchElementException("No data received from the API.")
        } else {
            throw Exception("Network request failed: ${response.errorBody()?.string() ?: "Unknown error"}")
        }
    }

    private suspend fun makeNetworkRequest(query: String) = withContext(Dispatchers.IO) {
        apiInterface.getApiResponse(query)
    }

    private suspend fun cacheResponse(
        query: String,
        data: String,
        timestamp: Long,
        cacheKey: String
    ) = withContext(Dispatchers.IO) {
        val newCacheEntity = CacheEntity(query, data, timestamp)
        cacheDao.insertResponse(newCacheEntity)
        InMemoryCache.save(cacheKey, data)
        Log.d("DataRepository", "Data cached for query: $query")
    }

    /**
     * Deletes expired cache entries from the local database.
     */
    suspend fun cleanupExpiredCacheEntries() = withContext(Dispatchers.IO) {
        val deletedRows = cacheDao.deleteExpiredResponses(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30))
        Log.d("DataRepository", "Expired cache entries deleted: $deletedRows rows affected.")
    }

    /**
     * Extension function on CacheDao to determine if a cached response is still valid.
     */
    suspend fun CacheDao.getCachedResponseIfValid(
        query: String,
        validityDurationMillis: Long
    ): CacheEntity? {
        val currentTime = System.currentTimeMillis()
        val validTime = currentTime - validityDurationMillis
        deleteExpiredResponses(currentTime - TimeUnit.MINUTES.toMillis(30)) // Cleanup old entries
        return getCachedResponse(query)?.takeIf { it.timestamp > validTime }
    }
}

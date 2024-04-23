package com.example.quickquery.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import android.util.Log

/**
 * Data Access Object for the cache operations related to CacheEntity.
 */
@Dao
interface CacheDao {
    /**
     * Retrieves a cached response based on the query string.
     *
     * @param query The query string whose associated cached response is to be returned.
     * @return The cached response if found, null otherwise.
     */
    @Query("SELECT * FROM responses WHERE `query` = :query")
    suspend fun getCachedResponse(query: String): CacheEntity?

    /**
     * Inserts a new response or replaces an existing one in the database.
     *
     * @param cacheEntity The cache entity to be inserted or replaced.
     * @return The row ID of the newly inserted data, or the number of rows affected when replacing.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(cacheEntity: CacheEntity): Long?

    /**
     * Deletes cached responses that are older than a specified timestamp.
     *
     * @param expirationTime The timestamp before which all responses should be deleted.
     * @return The number of rows deleted from the database.
     */
    @Query("DELETE FROM responses WHERE timestamp < :expirationTime")
    suspend fun deleteExpiredResponses(expirationTime: Long): Int?

    @Query("SELECT * FROM responses")
    suspend fun getAllCacheEntries(): List<CacheEntity>

}


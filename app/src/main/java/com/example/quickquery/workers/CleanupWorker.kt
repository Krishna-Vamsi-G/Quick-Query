package com.example.quickquery.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.quickquery.model.local.AppDatabase
import com.example.quickquery.utils.InMemoryCache
import java.util.concurrent.TimeUnit

/**
 * Worker class that handles cleanup of old cache entries in both the SQLite database and in-memory LRU cache.
 */
class CleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "CleanupWorker"
        private const val EXPIRATION_TIME_MINUTES = 30L // Time after which cache entries should be considered expired
    }

    /**
     * Performs the cleanup work by deleting expired entries from both the database and the in-memory cache.
     *
     * @return Result which can be Result.success() or Result.failure()
     */
    override suspend fun doWork(): Result {
        // Define the cutoff time for expired entries
        val expirationTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(EXPIRATION_TIME_MINUTES)

        return try {
            // Clean up the database
            val dao = AppDatabase.getInstance(applicationContext).cacheDao()
            val deletedDbRows = dao.deleteExpiredResponses(expirationTime)
            Log.d(TAG, "Cleaned up $deletedDbRows expired entries from the database.")

            // Clean up the in-memory cache
            val deletedCacheEntries = InMemoryCache.cleanup(expirationTime)
            Log.d(TAG, "Cleaned up $deletedCacheEntries expired entries from the in-memory cache.")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up the cache and database", e)
            Result.failure()
        }
    }
}

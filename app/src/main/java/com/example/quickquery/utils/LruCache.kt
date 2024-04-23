package com.example.quickquery.utils

import android.util.LruCache
import android.util.Log

/**
 * A utility object to manage in-memory caching of data. It utilizes LruCache to store and retrieve data efficiently.
 */
object InMemoryCache {
    private const val TAG = "InMemoryCache" // Tag used for logging
    data class TimedEntry(val data: String, val timestamp: Long)

    // Initialize LRU cache with a max size. Here, 1024 entries are allowed before evicting.
    private val lruCache: LruCache<String, TimedEntry> = LruCache(1024)

    /**
     * Saves a string data associated with a key in the cache.
     *
     * @param key The key with which the specified value is to be associated.
     * @param data The data to be associated with the specified key.
     */
    fun save(key: String, data: String) {
        try {
            val entry = TimedEntry(data, System.currentTimeMillis())
            lruCache.put(key, entry)
            Log.d(TAG, "Data saved in cache for key: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving data in cache for key: $key", e)
        }
    }

    /**
     * Loads the string data associated with the key if it is still valid.
     *
     * @param key The key whose associated value is to be returned.
     * @param validDurationMillis The valid duration in milliseconds for which the cache entry is considered valid.
     * @return The data associated with the key if present and valid; null otherwise.
     */
    fun load(key: String, validDurationMillis: Long): String? {
        return try {
            val entry = lruCache.get(key)
            if (entry != null && System.currentTimeMillis() - entry.timestamp < validDurationMillis) {
                Log.d(TAG, "Data loaded from cache for key: $key")
                entry.data
            } else {
                lruCache.remove(key)
                Log.d(TAG, "Cache entry expired or not found for key: $key")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading data from cache for key: $key", e)
            null
        }
    }

    /**
     * Clears all entries from the cache.
     */
    fun clear() {
        try {
            lruCache.evictAll()
            Log.d(TAG, "Cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }

    /**
     * Returns a snapshot of the cache's current entries. Useful for debugging or displaying cache contents.
     * This method captures a read-only view of the cache entries at a specific point in time.
     *
     * @return A map of cache keys to their respective timed entries.
     */
    fun snapshot(): Map<String, TimedEntry> {
        return try {
            val map = mutableMapOf<String, TimedEntry>()
            lruCache.snapshot().forEach { (key, value) ->
                map[key] = value
                Log.d(TAG, "Snapshot entry: key=$key, data=${value.data}, timestamp=${value.timestamp}")
            }
            map // Successfully returning the snapshot map.
        } catch (e: Exception) {
            Log.e(TAG, "Error taking snapshot of the cache", e)
            emptyMap() // Returning an empty map in case of error.
        }
    }

    /**
     * Cleans up expired entries from the cache.
     *
     * @param cutoffTime The timestamp before which cache entries are considered expired.
     * @return The number of entries removed.
     */
    fun cleanup(cutoffTime: Long): Int {
        val keysToRemove = mutableListOf<String>()
        lruCache.snapshot().forEach { (key, value) ->
            if (value.timestamp < cutoffTime) {
                keysToRemove.add(key)
            }
        }

        // Remove the identified expired entries
        keysToRemove.forEach { lruCache.remove(it) }
        Log.d(TAG, "Removed ${keysToRemove.size} expired entries from cache.")
        return keysToRemove.size
    }
}

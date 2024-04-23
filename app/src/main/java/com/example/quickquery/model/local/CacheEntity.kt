package com.example.quickquery.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "responses")
data class CacheEntity(
    @PrimaryKey val query: String,  // Unique key for each cache entry.
    val response: String,           // JSON response from the API.
    val timestamp: Long             // Time when the entry was cached.
)

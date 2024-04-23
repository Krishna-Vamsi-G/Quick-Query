package com.example.quickquery.repository

enum class DataSourceType {
    CACHE,    // Indicates data is loaded from in-memory cache.
    DATABASE, // Indicates data is loaded from persistent storage/database.
    NETWORK   // Indicates data is loaded from a network request.
}
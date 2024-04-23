package com.example.quickquery.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.lang.Exception

/**
 * Main database description for the application.
 *
 * @Database marks the class as a Room Database with a table (entity) of CacheEntity.
 * @param entities List of all entities included in the database. All entities must be annotated with @Entity.
 * @param version The current version of the database. Each version must be greater than the previous.
 * @param exportSchema Boolean value indicating whether to export the database schema JSON files to the project folder.
 */
@Database(entities = [CacheEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Retrieves the DAO that offers access to the underlying SQLite operations for the CacheEntity table.
     */
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Singleton pattern to get the instance of the database.
         *
         * @param context The application context.
         * @return The singleton instance of AppDatabase.
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        /**
         * Builds the database configuration and returns a new instance of AppDatabase.
         *
         * @param context The application context.
         * @return A new instance of AppDatabase.
         */
        private fun buildDatabase(context: Context) = try {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "quick_query_db"
            )
                .fallbackToDestructiveMigration() // Handle schema changes without migration scripts.
                .build()
        } catch (e: Exception) {
            throw RuntimeException("Error building database: ${e.message}")
        }
    }
}

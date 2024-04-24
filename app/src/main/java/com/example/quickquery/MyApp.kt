package com.example.quickquery

import android.app.Application
import android.util.Log
import androidx.work.*
import com.example.quickquery.api.ApiInterface
import com.example.quickquery.model.local.AppDatabase
import com.example.quickquery.repository.DataRepository
import com.example.quickquery.workers.CleanupWorker
import java.util.concurrent.TimeUnit

class MyApp : Application(), Configuration.Provider {
    lateinit var repository: DataRepository  // Holds the repository for accessing data operations

    override fun onCreate() {
        super.onCreate()
        // Initialize the data repository which setups the API service and the database
        initializeRepository()
        // Schedule periodic cleanup tasks using WorkManager to maintain database health
        schedulePeriodicCleanup()
    }

    /**
     * Initializes the data repository with dependencies required for operation.
     * This includes setting up the Retrofit service and the Room database.
     */
    private fun initializeRepository() {
        try {
            val apiService = ApiInterface.create()  // Creates an instance of Retrofit API service
            val appDatabase = AppDatabase.getInstance(this)  // Retrieves a singleton instance of the Room database
            repository = DataRepository(apiService, appDatabase.cacheDao())  // Instantiates the repository with the API service and DAO
        } catch (e: Exception) {
            Log.e("MyApp", "Initialization failed: ${e.message}")
        }
    }

    /**
     * Configures and does the periodic and immediate cleanup jobs using WorkManager.
     * This method sets up two types of work requests: immediate and periodic.
     */
    private fun schedulePeriodicCleanup() {
        val workManager = WorkManager.getInstance(this)

        // Setup immediate cleanup to run as soon as possible
        val immediateCleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
            .build()
        workManager.enqueueUniqueWork(
            "immediateCleanupJob",
            ExistingWorkPolicy.REPLACE,  // Ensures this job doesn't duplicate if already scheduled
            immediateCleanupRequest
        )

        // Setup periodic cleanup to run every 30 minutes, starting 30 minutes after app start
        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupWorker>(30, TimeUnit.MINUTES)
            .setInitialDelay(30, TimeUnit.MINUTES)  // Delays the initial execution to 30 minutes after launch
            .build()
        workManager.enqueueUniquePeriodicWork(
            "periodicCleanupJob",
            ExistingPeriodicWorkPolicy.KEEP,  // Keeps the job as is if it's already scheduled
            cleanupRequest
        )
    }

    /**
     * Provides the WorkManager with custom configuration options.
     * This configuration sets the logging level to DEBUG to help in development and troubleshooting.
     */
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)  // Sets log level to debug for detailed output
            .build()
}

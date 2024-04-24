package com.example.quickquery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import com.example.quickquery.repository.DataRepository

/**
 * Factory for ViewModels to inject dependencies needed for creating instances.
 * @param repository The DataRepository instance that will be passed to the ViewModel.
 */
class ViewModelFactory(private val repository: DataRepository) : ViewModelProvider.Factory {

    companion object {
        private const val TAG = "ViewModelFactory"
    }

    /**
     * Creates a new instance of the given `ViewModel` class.
     * @param modelClass A `Class` whose instance is desired.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if there is no known way to create an instance.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d(TAG, "Attempting to create a ViewModel.")
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                // Cast the ViewModel to the type expected by the consumer.
                @Suppress("UNCHECKED_CAST")
                MainViewModel(repository) as T
            }
            else -> {
                // Log the error and throw an exception if the ViewModel class is not supported.
                Log.e(TAG, "Unknown ViewModel class: ${modelClass.name}")
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}

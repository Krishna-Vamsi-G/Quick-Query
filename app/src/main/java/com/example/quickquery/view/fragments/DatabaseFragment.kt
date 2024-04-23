package com.example.quickquery.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.quickquery.databinding.FragmentDatabaseBinding
import com.example.quickquery.model.local.AppDatabase
import com.example.quickquery.model.local.CacheEntity
import com.example.quickquery.view.adapters.CountryAdapter
import com.example.quickquery.workers.CleanupWorker
import kotlinx.coroutines.launch

class DatabaseFragment : Fragment() {

    private var _binding: FragmentDatabaseBinding? = null

    // Safe access to the binding property to avoid memory leaks.
    private val binding get() = _binding!!

    /**
     * Inflates the layout for this fragment and initializes binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatabaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called after the fragment's view has been created. This is where we start the cleanup process and load data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initiateCleanupAndLoadData()  // Start the cleanup process and load the data afterwards.
    }

    /**
     * Ensures the binding is nullified when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Initiates the cleanup process using WorkManager and subsequently loads the data once the cleanup is complete.
     */
    private fun initiateCleanupAndLoadData() {
        // Create a one-time request to cleanup old cache entries.
        val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>().build()
        WorkManager.getInstance(requireContext()).enqueue(cleanupRequest)

        // Observe the result of the cleanup work and load data upon completion.
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(cleanupRequest.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    loadData()  // Load data from the database once cleanup is complete.
                }
            }
    }

    /**
     * Loads data from the local database and updates the RecyclerView adapter.
     */
    private fun loadData() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getInstance(requireContext()).cacheDao()
                val dataList =
                    database.getAllCacheEntries()  // Fetch all cache entries from the database.

                binding.rvDatabase.apply {
                    noDataView(dataList)  // Display or hide the "No Data" view based on the entries count.
                    layoutManager = LinearLayoutManager(context)
                    adapter = CountryAdapter(dataList)  // Set adapter with the fetched data.
                }
            } catch (e: Exception) {
                Log.e("DatabaseFragment", "Error loading data: ${e.message}")
                showNoDataView()  // Show "No Data" view in case of error.
            }
        }
    }

    /**
     * Determines whether to show or hide the "No Data" view based on the list's size.
     *
     * @param dataList List of CacheEntity objects to check.
     */
    private fun noDataView(dataList: List<CacheEntity>) {
        if (dataList.isEmpty()) {
            binding.imageView.visibility = View.VISIBLE
            binding.textView.visibility = View.VISIBLE
            Log.d("DatabaseFragment", "No data available. Showing no data view.")
        } else {
            binding.imageView.visibility = View.GONE
            binding.textView.visibility = View.GONE
        }
    }

    /**
     * Shows the "No Data" view.
     */
    private fun showNoDataView() {
        binding.imageView.visibility = View.VISIBLE
        binding.textView.visibility = View.VISIBLE
        Log.d("DatabaseFragment", "An error occurred. Showing no data view.")
    }
}

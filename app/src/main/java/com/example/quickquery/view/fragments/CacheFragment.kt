package com.example.quickquery.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quickquery.databinding.FragmentCacheBinding
import com.example.quickquery.model.local.CacheEntity
import com.example.quickquery.utils.InMemoryCache
import com.example.quickquery.view.adapters.CountryAdapter
import kotlinx.coroutines.launch

class CacheFragment : Fragment() {

    private var _binding: FragmentCacheBinding? = null

    // Safe access to the binding property to avoid memory leaks.
    private val binding get() = _binding!!

    /**
     * Inflates the layout for this fragment and initializes binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCacheBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called after the fragment's view has been created. This is where we initialize the cache display.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCacheDisplay()  // Setup cache display by fetching and displaying entries from LRU cache.
    }

    /**
     * Sets up the display of cache entries using a RecyclerView.
     */
    private fun setupCacheDisplay() {
        lifecycleScope.launch {
            try {
                // Collect all entries from the LRU cache into a list of CacheEntity
                val entries = mutableListOf<CacheEntity>()
                InMemoryCache.snapshot().forEach { (key, value) ->
                    entries.add(
                        CacheEntity(
                            query = key,
                            response = value.data,
                            timestamp = value.timestamp
                        )
                    )
                }

                binding.rvDatabase.apply {
                    noDataView(entries)  // Display or hide the "No Data" view based on the entries count.
                    layoutManager = LinearLayoutManager(context)
                    adapter =
                        CountryAdapter(entries)  // Set adapter with the fetched cache entries.
                }
            } catch (e: Exception) {
                Log.e("CacheFragment", "Error setting up cache display: ${e.message}")
                showNoDataView()  // Show "No Data" view in case of error.
            }
        }
    }

    /**
     * Ensures the binding is nullified when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            Log.d("CacheFragment", "No cache entries available. Showing no data view.")
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
        Log.d("CacheFragment", "An error occurred. Showing no data view.")
    }
}

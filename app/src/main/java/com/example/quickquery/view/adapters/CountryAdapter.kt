package com.example.quickquery.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quickquery.R
import com.example.quickquery.model.local.CacheEntity
import com.example.quickquery.model.response.Country
import com.example.quickquery.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log

/**
 * Adapter for displaying country data in a RecyclerView.
 *
 * @param dataList The list of CacheEntity objects containing serialized country data.
 */
class CountryAdapter(private val dataList: List<CacheEntity>) :
    RecyclerView.Adapter<CountryAdapter.DataViewHolder>() {

    private val gson = Gson() // Gson instance for deserializing JSON data.

    /**
     * Provides a reference to the views for each data item.
     */
    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCountryName: TextView = itemView.findViewById(R.id.countryName)
        val tvCapital: TextView = itemView.findViewById(R.id.countryCapital)
        val tvFlag: TextView = itemView.findViewById(R.id.countryFlag)
    }

    /**
     * Create new views.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.country_item, parent, false)
        return DataViewHolder(itemView)
    }

    /**
     * Returns the size of your dataset.
     */
    override fun getItemCount(): Int = dataList.size

    /**
     * Replace the contents of a view.
     */
    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val item = dataList[position]

        try {
            // Deserialize JSON string back into Country objects.
            val type = object : TypeToken<List<Country>>() {}.type
            val countries: List<Country>? = gson.fromJson(item.response, type)
            val formattedTime = Utils.convertMillisToReadableDate(item.timestamp)

            // Display the first country's details if available.
            countries?.firstOrNull()?.let { country ->
                holder.tvCountryName.text = country.name?.common ?: "N/A"
                holder.tvCapital.text = "${country.capital?.joinToString() ?: "No Capital"}, $formattedTime"
                holder.tvFlag.text = country.flag ?: "🏳️" // Default flag if none available.
            } ?: run {
                Log.e("CountryAdapter", "No country data available for position $position")
                holder.tvCountryName.text = "N/A"
                holder.tvCapital.text = "N/A, $formattedTime"
                holder.tvFlag.text = "🏳️"
            }
        } catch (e: Exception) {
            Log.e("CountryAdapter", "Error parsing data at position $position: ${e.message}")
            holder.tvCountryName.text = "Error"
            holder.tvCapital.text = "Error"
            holder.tvFlag.text = "🏳️"
        }
    }
}

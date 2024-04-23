package com.example.quickquery.model.response

// Represents the naming schema of a country with optional native names.
data class Name(
    val common: String?,  // The common name of the country in English.
    val official: String?,  // The official name of the country.
    val nativeName: Map<String, Map<String, String>>?  // Native names keyed by language codes.
)

// Represents a currency with its name and symbol.
data class Currency(
    val name: String?,  // Name of the currency.
    val symbol: String?  // Symbol used for the currency, e.g., $, €, etc.
)

// Main data class representing a country with comprehensive details.
data class Country(
    val name: Name?,  // Name details of the country.
    val tld: List<String>?,  // Top-level domain(s) of the country.
    val cca2: String?,  // Two-letter country code.
    val ccn3: String?,  // Three-number country code.
    val cca3: String?,  // Three-letter country code.
    val cioc: String?,  // International Olympic Committee code.
    val independent: Boolean?,  // Whether the country is independent.
    val status: String?,  // Status of the country.
    val unMember: Boolean?,  // UN membership status.
    val currencies: Map<String, Currency>?,  // Currencies used by the country.
    val capital: List<String>?,  // Capital city or cities.
    val region: String?,  // Geographical region.
    val subregion: String?,  // Geographical sub-region.
    val languages: Map<String, String>?,  // Languages spoken.
    val translations: Map<String, Map<String, String>>?,  // Translations of the country's name.
    val latlng: List<Double>?,  // Latitude and longitude.
    val landlocked: Boolean?,  // Whether the country is landlocked.
    val borders: List<String>?,  // Adjacent countries by their codes.
    val area: Double?,  // Total area in square kilometers.
    val demonyms: Map<String, Map<String, String>>?,  // Demonyms for the country.
    val flag: String?,  // Emoji flag of the country.
    val maps: Map<String, String>?,  // Map URLs.
    val population: Long?,  // Total population.
    val gini: Map<String, Double>?,  // GINI index per year.
    val fifa: String?,  // FIFA code.
    val car: Map<String, Any>?,  // Car driving details like side of the road.
    val timezones: List<String>?,  // List of all time zones applicable to the country.
    val continents: List<String>?,  // Continents that the country is part of.
    val flags: Map<String, String>?,  // URL to images of the flag.
    val coatOfArms: Map<String, String>?,  // Coat of arms image URLs.
    val startOfWeek: String?,  // Which day the week starts on.
    val capitalInfo: Map<String, List<Double>>?,  // Additional capital information like coordinates.
    val postalCode: Map<String, String>?  // Postal code regex pattern.
)

// Type alias for list of Country, simplifies use in network responses.
typealias ApiResponse = List<Country>

package com.example.esportsevent

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val favoritesKey = "favorites"

    fun getFavorites(): List<Match> {
        val json = sharedPreferences.getString(favoritesKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<Match>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun toggleFavorite(match: Match) {
        val currentFavorites = getFavorites().toMutableList()
        if (currentFavorites.contains(match)) {
            currentFavorites.remove(match)
        } else {
            currentFavorites.add(match)
            currentFavorites.sortBy { it.fixture.timestamp }
        }
        val json = gson.toJson(currentFavorites)
        sharedPreferences.edit().putString(favoritesKey, json).apply()
    }

}
package com.example.esportsevent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MatchDetailViewModel : ViewModel() {
    private val _homeTeamStatistics = MutableLiveData<TeamStatisticsResponse>()
    val homeTeamStatistics: LiveData<TeamStatisticsResponse> = _homeTeamStatistics

    private val _awayTeamStatistics = MutableLiveData<TeamStatisticsResponse>()
    val awayTeamStatistics: LiveData<TeamStatisticsResponse> = _awayTeamStatistics

    fun fetchTeamStatistics(homeTeamId: Int, awayTeamId: Int, leagueId: Int, season: Int) {
        fetchStatistics(homeTeamId, leagueId, season, _homeTeamStatistics)
        fetchStatistics(awayTeamId, leagueId, season, _awayTeamStatistics)
    }

    fun fetchStatistics(teamId: Int, leagueId: Int, season: Int, liveData: MutableLiveData<TeamStatisticsResponse>) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api-football-v1.p.rapidapi.com/v3/teams/statistics?league=$leagueId&season=$season&team=$teamId")
                .get()
                .addHeader("X-RapidAPI-Key", "f553732919msh90339d7020d3a54p16b6dfjsnae421a10c7ac")
                .addHeader("X-RapidAPI-Host", "api-football-v1.p.rapidapi.com")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val teamStats = Gson().fromJson(responseBody, TeamStatisticsResponse::class.java)
                    withContext(Dispatchers.Main) {
                        liveData.postValue(teamStats)
                    }
                } else {
                    Log.e("API Error", "Response not successful or body is null")
                }
            } catch (e: Exception) {
                Log.e("API Error", "Failed to fetch data: ${e.message}")
            }
        }
    }
}

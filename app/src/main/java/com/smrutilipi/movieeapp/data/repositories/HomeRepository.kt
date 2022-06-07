package com.smrutilipi.movieeapp.data.repositories

import com.smrutilipi.movieeapp.data.model.SearchResults
import com.smrutilipi.movieeapp.data.network.ApiInterface
import com.smrutilipi.movieeapp.data.network.SafeApiRequest

class HomeRepository(
    private val api: ApiInterface
) : SafeApiRequest() {

    suspend fun getMovies(
        searchTitle: String,
        apiKey: String,
        pageIndex: Int
    ): SearchResults {

        return apiRequest { api.getSearchResultData(searchTitle, apiKey, pageIndex) }
    }


}
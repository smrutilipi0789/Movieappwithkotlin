package com.smrutilipi.movieeapp.data.repositories

import com.smrutilipi.movieeapp.data.model.MovieDetail
import com.smrutilipi.movieeapp.data.network.ApiInterface
import com.smrutilipi.movieeapp.data.network.SafeApiRequest

class MovieDetailRepository(
    private val api: ApiInterface
) : SafeApiRequest() {

    suspend fun getMovieDetail(
        title: String,
        apiKey: String
    ): MovieDetail {

        return apiRequest { api.getMovieDetailData(title, apiKey) }
    }


}
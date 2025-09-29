package com.unsoed.moviesta.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val instance: TmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(TmdbApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }
}
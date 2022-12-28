package com.example.ezanvakti

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PostService{
    @GET(/* value = */ "sehirler/2")//sehirler
    fun listCities(@Query("SehirID") SehirID: String): Call<List<Cities>>
    @GET(/* value = */ "ilceler/{path}")//ilceler
    fun listTowns(@Path("path") path: String): Call<List<Towns>>
    @GET(/* value = */ "vakitler/{path}")//vakitler
    fun listPrayerTime(@Path("path") path: String): Call<List<PrayerTime>>
}
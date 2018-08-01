package com.example.edward.dewnet.api

import android.util.Log
import com.example.edward.dewnet.model.YoutubeResponse
import com.example.edward.dewnet.util.API_KEY
import com.example.edward.dewnet.util.NETWORK_PAGE_SIZE
import com.example.edward.dewnet.util.YOUTUBE_BASE_URL
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Created by Edward on 7/31/2018.
 */

interface YoutubeAPI {
    @GET("search")
    fun searchVideo(@Query("q") query: String = "",
                    @Query("pageToken") pageToken: String ="",
                    @Query("part") part: String = "snippet",
                    @Query("maxResults") maxResults: String = "$NETWORK_PAGE_SIZE",
                    @Query("type") type: String = "video",
                    @Query("key") key: String = API_KEY): Call<YoutubeResponse>

    @GET("search")
    fun getRelatedVideos(@Query("relatedToVideoId") relatedToVideoId: String = "",
                         @Query("pageToken") pageToken: String = "",
                         @Query("part") part: String = "snippet",
                         @Query("maxResults") maxResults: String = "$NETWORK_PAGE_SIZE",
                         @Query("type") type: String = "video",
                         @Query("key") key: String = API_KEY): Call<YoutubeResponse>


    @Streaming
    @GET
    fun downloadByUrlStream(@Url url: String): Call<ResponseBody>

    companion object {

        fun create(): YoutubeAPI = createYoutubeApi(HttpUrl.parse(YOUTUBE_BASE_URL)!!)
        private fun createYoutubeApi(httpUrl: HttpUrl): YoutubeAPI {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("YoutubeAPI", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val okHttpClient = OkHttpClient.Builder().addInterceptor(logger).build()

            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(YoutubeAPI::class.java)
        }
    }
}
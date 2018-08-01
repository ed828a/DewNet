package com.example.edward.dewnet.api

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import android.util.Log
import com.example.edward.dewnet.model.*
import com.example.edward.dewnet.util.extractDate
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor

/**
 * Created by Edward on 7/31/2018.
 */

class PageKeyedYoutubeDataSource(private val youtubeApi: YoutubeAPI,
                                 private val searchQuery: QueryData,
                                 private val retryExecutor: Executor) : PageKeyedDataSource<String, VideoModel>() {
    private val tag = "PageKeyedDataSource"

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    val resultPageInfo = ResultPageInfo()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadInitial(
            params: LoadInitialParams<String>,
            callback: LoadInitialCallback<String, VideoModel>) {

        val request = if (searchQuery.type == Type.QUERY_STRING) {
            youtubeApi.searchVideo(query = searchQuery.query)
        } else {
            youtubeApi.getRelatedVideos(relatedToVideoId = searchQuery.query)
        }

        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        resultPageInfo.reset()


        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            val items = processResponse(response, "loadInitial")
            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)

            callback.onResult(items, null, resultPageInfo.nextPage)
            Log.d(tag, "loadInitial: nextPageToken = ${resultPageInfo.nextPage}, receivedItems: ${resultPageInfo.receivedItems} of ${resultPageInfo.totalResults}")

        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadBefore(
            params: LoadParams<String>,
            callback: LoadCallback<String, VideoModel>) {
        // ignored, since we only ever append to our initial load
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, VideoModel>) {
        networkState.postValue(NetworkState.LOADING)

        if (resultPageInfo.nextPage != params.key) {
            Log.d(tag, "loadAfter, resultPageInfo.nextPage:${resultPageInfo.nextPage} != params.key: ${params.key}")
        }
        val call = if (searchQuery.type == Type.QUERY_STRING) {
            youtubeApi.searchVideo(query = searchQuery.query, pageToken = params.key)
        } else {
            youtubeApi.getRelatedVideos(relatedToVideoId = searchQuery.query, pageToken = params.key)
        }
        call.enqueue(
                object : retrofit2.Callback<YoutubeResponse> {
                    override fun onFailure(call: Call<YoutubeResponse>, t: Throwable) {
                        retry = {
                            loadAfter(params, callback)
                        }
                        networkState.postValue(NetworkState.error(t.message ?: "unknown err"))
                    }

                    override fun onResponse(call: Call<YoutubeResponse>,
                                            response: Response<YoutubeResponse>) {
                        if (response.isSuccessful) {
                            val items = processResponse(response, "loadAfter")
                            retry = null

                            callback.onResult(items, resultPageInfo.nextPage)

                            networkState.postValue(NetworkState.LOADED)
                            Log.d("loadAfter", "nextPageToken: ${resultPageInfo.nextPage}, receivedItems: ${resultPageInfo.receivedItems} of ${resultPageInfo.totalResults}")
                        } else {
                            retry = {
                                loadAfter(params, callback)
                            }
                            networkState.postValue(
                                    NetworkState.error("error code: ${response.code()}"))
                        }
                    }
                }
        )
    }


    private fun processResponse(response: Response<YoutubeResponse>, functionName: String): List<VideoModel> {

        val data = response.body()
        val items: List<VideoModel> = data?.items?.map {
            VideoModel(
                    it.snippet.title,
                    it.snippet.publishedAt.extractDate(),
                    it.snippet.thumbnails.high.url, it.id.videoId
            )
        } ?: emptyList()

        // update pageTokens
        with(resultPageInfo) {
            prevPage = data?.prevPageToken ?: ""
            nextPage = data?.nextPageToken ?: ""
            totalResults = data?.pageInfo?.totalResults ?: ""
            if (items.isNotEmpty()) {
                receivedItems += items.size
            } else {
                android.util.Log.d(tag, "$functionName response: items are empty")
            }
        }

        return items
    }

}
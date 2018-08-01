package com.example.edward.dewnet.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.os.Environment
import android.support.annotation.MainThread
import android.util.Log
import com.example.edward.dewnet.api.YoutubeAPI
import com.example.edward.dewnet.api.YoutubeDataSourceFactory
import com.example.edward.dewnet.model.LiveDataPagedListing
import com.example.edward.dewnet.model.NetworkState
import com.example.edward.dewnet.model.QueryData
import com.example.edward.dewnet.model.VideoModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.concurrent.Executor

/**
 * Created by Edward on 7/31/2018.
 */

class PageKeyedRepository(
        private val youtubeApi: YoutubeAPI,
        private val networkExecutor: Executor) : DewRepository {
    val tag = "PageKeyedRepository"
    val downloadingState = MutableLiveData<Boolean>()

    override val isSucessful: LiveData<Boolean>
        get() = downloadingState


    @MainThread
    override fun searchVideoYoutube(searchYoutube: QueryData, pageSize: Int): LiveDataPagedListing<VideoModel> {
        val sourceFactory = YoutubeDataSourceFactory(youtubeApi, searchYoutube, networkExecutor)

        val livePagedList = LivePagedListBuilder(sourceFactory, pageSize)
                // provide custom executor for network requests, otherwise it will default to
                // Arch Components' IO pool which is also used for disk access
                .setFetchExecutor(networkExecutor)
                .build()

        return LiveDataPagedListing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value.apply {
                        this?.initialLoad?.postValue(NetworkState.LOADED)
                    }
                },
                refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.initialLoad
                }
        )
    }


    override fun downloading(urlString: String, fileName: String) {
        val call = youtubeApi.downloadByUrlStream(urlString)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                t?.printStackTrace()
                Log.d(tag, "downloading failed: ${t?.message}")
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response != null && response.isSuccessful) {
                    networkExecutor.execute {
                        val isFinished = writeResponseBodyToDisk(fileName, response.body()!!)
                        downloadingState.postValue(isFinished)
                        Log.d(tag, "downloading completed successfully.")
                    }
                } else {
                    downloadingState.postValue(false)
                    Log.d(tag, "Response Error: ${response?.message()}")
                }
            }
        })
    }

    private fun writeResponseBodyToDisk(fileName: String, responseBody: ResponseBody): Boolean {
        val fileFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Log.d(tag, "filename = $fileFolder/$fileName")
        val file = File(fileFolder, "$fileName.mp4")
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)
            val fileSize = responseBody.contentLength()
            var fileSizeDownloaded: Long = 0

            inputStream = responseBody.byteStream()
            outputStream = FileOutputStream(file)

            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
                fileSizeDownloaded += read.toLong()

                Log.d(tag, "Downloading progress: $fileSizeDownloaded of $fileSize")
            }
            outputStream.flush()

            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }
}
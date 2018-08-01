package com.example.edward.dewnet.repository

import com.example.edward.dewnet.api.YoutubeAPI
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by Edward on 7/31/2018.
 */

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
class DefaultRepositoryLocator : DewRepositoryLocator {

    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val api by lazy {
        YoutubeAPI.create()
    }

    override fun getRepository(): DewRepository {
        return PageKeyedRepository(
                youtubeApi = getYoutubeApi(),
                networkExecutor = getNetworkExecutor())
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getYoutubeApi(): YoutubeAPI = api
}


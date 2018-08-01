package com.example.edward.dewnet.repository

import android.support.annotation.VisibleForTesting
import com.example.edward.dewnet.api.YoutubeAPI
import java.util.concurrent.Executor

/**
 * Created by Edward on 7/31/2018.
 */

/**
 * Super simplified service locator implementation to allow us to replace default implementations
 * for testing.
 */
interface DewRepositoryLocator {
    companion object {
        private val LOCK = Any()
        private var instance: DewRepositoryLocator? = null
        fun getInstance(): DewRepositoryLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultRepositoryLocator()
                }
                return instance!!
            }
        }

        /**
         * Allows tests to replace the default implementations.
         */
        @VisibleForTesting
        fun swap(locator: DewRepositoryLocator) {
            instance = locator
        }
    }

    fun getRepository(): DewRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getYoutubeApi(): YoutubeAPI
}

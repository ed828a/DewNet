package com.example.edward.dewnet.util

import android.arch.lifecycle.LiveData
import com.example.edward.dewnet.model.NetworkState

/**
 * Created by Edward on 7/31/2018.
 */

fun String.extractDate(): String {
    val stringArray = this.split('T')

    return stringArray[0]
}

private fun getErrorMessage(report: PagingRequestHelper.StatusReport): String {
    return PagingRequestHelper.RequestType.values().mapNotNull {
        report.getErrorFor(it)?.message
    }.first()
}

fun PagingRequestHelper.createStatusLiveData(): LiveData<NetworkState> {
    val liveData = android.arch.lifecycle.MutableLiveData<NetworkState>()
    addListener { report ->
        when {
            report.hasRunning() -> liveData.postValue(NetworkState.LOADING)
            report.hasError() -> liveData.postValue(
                    NetworkState.error(getErrorMessage(report)))
            else -> liveData.postValue(NetworkState.LOADED)
        }
    }
    return liveData
}

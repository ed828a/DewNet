package com.example.edward.dewnet.model

import android.arch.lifecycle.LiveData
import android.arch.paging.PagedList

/**
 * Created by Edward on 7/31/2018.
 */


class LiveDataPagedListing<T> (
        val pagedList: LiveData<PagedList<T>>,     // the LiveData of paged lists for the UI to observe
        val networkState: LiveData<NetworkState>,  // represents the network status to show to the user
        val refreshState: LiveData<NetworkState>,  // represents the refresh status to show to the user
        val refresh: () -> Unit,                   // refreshes the whole data and fetches it from scratch.
        val retry: () -> Unit                      // retries any failed requests
)
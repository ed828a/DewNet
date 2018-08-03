package com.example.edward.dewnet.viewmodel

import android.arch.lifecycle.*
import com.example.edward.dewnet.model.QueryData
import com.example.edward.dewnet.model.Type
import com.example.edward.dewnet.repository.DewRepository
import com.example.edward.dewnet.util.PAGEDLIST_PAGE_SIZE

/**
 * Created by Edward on 7/31/2018.
 */


open class MainViewModel(protected val repository: DewRepository) : ViewModel(){
    protected val queryString = MutableLiveData<String>()
    protected val relatedToVideoId = MutableLiveData<String>()
    protected var queryData = MediatorLiveData<QueryData>()
    init {
        queryData.addSource(relatedToVideoId) { related ->
            queryData.value = QueryData(related ?: "", Type.RELATED_VIDEO_ID)
        }

        queryData.addSource(queryString) {query ->
            queryData.value = QueryData(query ?: "", Type.QUERY_STRING)
        }
    }

    private val searchResult =
            Transformations.map(queryData) { queryData ->
                repository.searchVideoYoutube(queryData, PAGEDLIST_PAGE_SIZE)
            }

    val videoList = Transformations.switchMap(searchResult){ it.pagedList}
    val networkState = Transformations.switchMap(searchResult){ it.networkState}
    val refreshState = Transformations.switchMap(searchResult){it.refreshState}

    fun refresh() {
        searchResult.value?.refresh?.invoke()
    }

    fun showSearchQuery(searchQuery: String): Boolean =
            if (queryString.value == searchQuery) false // repeating query
            else {
                queryString.value = searchQuery   // new query
                true
            }

    fun retry(){
        searchResult?.value?.retry?.invoke()
    }

    fun currentQuery(): String? = queryString.value
}
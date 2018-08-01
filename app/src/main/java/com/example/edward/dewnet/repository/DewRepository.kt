package com.example.edward.dewnet.repository

import android.arch.lifecycle.LiveData
import com.example.edward.dewnet.model.LiveDataPagedListing
import com.example.edward.dewnet.model.QueryData
import com.example.edward.dewnet.model.VideoModel

/**
 * Created by Edward on 7/31/2018.
 */

interface DewRepository {

    fun searchVideoYoutube(searchYoutube: QueryData, pageSize: Int): LiveDataPagedListing<VideoModel>

    fun downloading(urlString: String, fileName: String)

    val isSucessful: LiveData<Boolean>
}
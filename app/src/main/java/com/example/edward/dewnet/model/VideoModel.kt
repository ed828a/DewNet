package com.example.edward.dewnet.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Edward on 7/31/2018.
 */

@Parcelize
data class VideoModel(var title: String = "",
                      var date: String = "",
                      var thumbnail: String = "",
                      var videoId: String = "",
                      var relatedToVideoId: String = "",
        // indexResponse: to be consistent with changing backend order
                      var indexResponse: Int = -1) : Parcelable
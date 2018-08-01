package com.example.edward.dewnet.util

import android.support.v7.util.DiffUtil
import com.example.edward.dewnet.BuildConfig
import com.example.edward.dewnet.model.VideoModel

/**
 * Created by Edward on 7/31/2018.
 */

const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"
const val NETWORK_PAGE_SIZE = 50  //Values must be within the range: [0, 50]
const val PAGEDLIST_PAGE_SIZE = 15  // should be 20
const val API_KEY = BuildConfig.YOUTUBE_API_KEY
const val KEY_QUERY = "query"
const val DEFAULT_QUERY = "trump"
const val VIDEO_MODEL = "video_model"
const val PLAYBACK_POSITION = "playback_position"
const val VIDEO_URL = "video_url"
const val PERMISSION_REQUEST = 88
const val BACKING_STEPS = 5


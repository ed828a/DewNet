package com.example.edward.dewnet.model

/**
 * Created by Edward on 7/31/2018.
 */

data class QueryData (val query: String = "", val type: Type = Type.QUERY_STRING)
enum class Type{
    QUERY_STRING,
    RELATED_VIDEO_ID
}
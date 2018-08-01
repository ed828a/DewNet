package com.example.edward.dewnet.model

/**
 * Created by Edward on 7/31/2018.
 */

data class ResultPageInfo(var prevPage: String = "",
                          var nextPage: String = "",
                          var totalResults: String = "",
                          var receivedItems: Int = 0) {

    fun reset() {
        prevPage = ""
        nextPage = ""
        totalResults = ""
        receivedItems = 0
    }
}
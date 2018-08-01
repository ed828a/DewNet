package com.example.edward.dewnet.viewmodel

import android.arch.lifecycle.LiveData
import com.example.edward.dewnet.model.QueryData
import com.example.edward.dewnet.model.VideoPlayedModel
import com.example.edward.dewnet.repository.DewRepository
import com.example.edward.dewnet.util.BACKING_STEPS
import com.example.edward.dewnet.util.DewStack

class VideoPlayViewModel(repository: DewRepository) : MainViewModel(repository) {

    val downloadingState: LiveData<Boolean> = repository.isSucessful
    val backListStack = DewStack<QueryData>(BACKING_STEPS)
    val playListStack = DewStack<VideoPlayedModel>(BACKING_STEPS)

    fun showRelatedToVideoId(videoId: String): Boolean =
            if (relatedToVideoId.value == videoId) false // repeat search
            else {
                relatedToVideoId.value = videoId  // new search
                true
            }

    fun download(videoUrl: String, title: String){
        repository.downloading(videoUrl, title)
    }
}

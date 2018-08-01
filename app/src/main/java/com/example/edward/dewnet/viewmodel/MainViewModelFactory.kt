package com.example.edward.dewnet.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.example.edward.dewnet.repository.DewRepository

/**
 * Created by Edward on 7/31/2018.
 */

class MainViewModelFactory(private val repository: DewRepository) : ViewModelProvider.Factory {
//    private val repository: DewRepository = DewRepositoryLocator.getInstance().getRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(repository) as T
            modelClass.isAssignableFrom(VideoPlayViewModel::class.java) -> VideoPlayViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }
}
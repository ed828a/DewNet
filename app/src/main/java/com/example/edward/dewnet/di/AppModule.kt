package com.example.edward.dewnet.di

import android.content.Context
import com.example.edward.dewnet.repository.DewRepository
import com.example.edward.dewnet.repository.DewRepositoryLocator
import com.example.edward.dewnet.ui.DewNetApp
import com.example.edward.dewnet.viewmodel.MainViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Edward on 7/31/2018.
 */

@Module
class AppModule(private val app: DewNetApp) {
    @Singleton
    @Provides
    fun provideContext(): Context = app


    @Singleton
    @Provides
    fun provideYoutubeRepository(): DewRepository =
            DewRepositoryLocator.getInstance().getRepository()
    //    private val repository: DewRepository = DewRepositoryLocator.getInstance().getRepository()

    @Singleton
    @Provides
    fun provideViewModelFactory(repository: DewRepository): MainViewModelFactory =
            MainViewModelFactory(repository)

}
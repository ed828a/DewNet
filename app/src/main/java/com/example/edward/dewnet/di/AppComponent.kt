package com.example.edward.dewnet.di

import com.example.edward.dewnet.ui.ExoVideoPlayActivity
import com.example.edward.dewnet.ui.MainActivity
import dagger.Component
import javax.inject.Singleton

/**
 * Created by Edward on 7/31/2018.
 */

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(target: MainActivity)
    fun inject(target: ExoVideoPlayActivity)
}
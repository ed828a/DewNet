package com.example.edward.dewnet.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.support.multidex.MultiDexApplication
import com.example.edward.dewnet.di.AppComponent
import com.example.edward.dewnet.di.AppModule
import com.example.edward.dewnet.di.DaggerAppComponent

/**
 * Created by Edward on 7/31/2018.
 */

class DewNetApp: MultiDexApplication(){

    companion object {
        lateinit var sharedPreferences: SharedPreferences
        lateinit var appComponent: AppComponent
    }



    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        sharedPreferences = getSharedPreferences("DewApp", Context.MODE_PRIVATE)
    }
}
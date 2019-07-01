package com.example.testandroidgitflow

import android.app.Application
import com.raxeltelematics.v2.sdk.Settings
import com.raxeltelematics.v2.sdk.TrackingApi

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val settings = Settings(
            isSensorFull = true,
            stopTrackingTimeout = Settings.stopTrackingTimeNormal,
            accuracy = Settings.accuracyNormal,
            autoStartOn = true
        )
        val api = TrackingApi.getInstance()
        api.initialize(this, settings)
        api.setDeviceID("93121063-89da-478a-92bd-7b456137c3d3")
    }
}
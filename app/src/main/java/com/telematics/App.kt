package com.telematics

import android.app.Application
import com.raxeltelematics.v2.sdk.Settings
import com.raxeltelematics.v2.sdk.TrackingApi

class App : Application() {

	override fun onCreate() {
		super.onCreate()

		val settings = Settings(
				stopTrackingTimeout = Settings.stopTrackingTimeHigh,
				accuracy = Settings.accuracyHigh,
				autoStartOn = true,
				elmOn = false,
				hfOn = true
		)
		val api = TrackingApi.getInstance()
		api.initialize(this, settings)
	}
}
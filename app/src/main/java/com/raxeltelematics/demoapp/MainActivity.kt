package com.raxeltelematics.demoapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.raxeltelematics.v2.sdk.TrackingApi
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceId.setText(YOUR_TOKEN)

        if (isNeedPermissions(this)) {
            requestPermissions(this)
        }

        val trackingApi = TrackingApi.getInstance()

        dashboardButton.setOnClickListener {
            if (trackingApi.isSdkEnabled()) startActivity(Intent(this, DashboardStatisticsActivity::class.java))
            else {
                if (deviceId.text.toString().isEmpty()) {
                    Toast.makeText(this, "Device ID is empty", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                trackingApi.setDeviceID(deviceId.text.toString())
                startActivity(Intent(this, DashboardStatisticsActivity::class.java))
            }
        }
        tracksButton.setOnClickListener {
            if (trackingApi.isSdkEnabled()) startActivity(Intent(this, TripsListActivity::class.java))
            else {
                if (deviceId.text.toString().isEmpty()) {
                    Toast.makeText(this, "Device ID is empty", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                trackingApi.setDeviceID(deviceId.text.toString())
                startActivity(Intent(this, TripsListActivity::class.java))
            }
        }

        enable_manual_sdk.setOnClickListener {
            if (deviceId.text.toString().isEmpty()) {
                Toast.makeText(this, "Device ID is empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            trackingApi.setDeviceID(deviceId.text.toString())
            trackingApi.setEnableSdk(true)
        }

        disable_manual_sdk.setOnClickListener {
            trackingApi.setEnableSdk(false)
//            trackingApi.clearDeviceID()
        }

        start.setOnClickListener {
            trackingApi.startTracking()
        }

        stop.setOnClickListener {
            trackingApi.stopTracking()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (isNeedPermissions(this)) {
            Toast.makeText(this, "Need permissions!", Toast.LENGTH_SHORT).show()
            requestPermissions(this)
        } else {
            if (deviceId.text.toString().isEmpty()) {
                Toast.makeText(this, "Device ID is empty", Toast.LENGTH_LONG).show()
                return
            }
            TrackingApi.getInstance().setDeviceID(deviceId.text.toString())
            TrackingApi.getInstance().setEnableSdk(true)
        }
    }

    fun isNeedPermissions(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        val backgroundLocation = false
        //        if (android.os.Build.VERSION.SDK_INT >= 29) {
        //            backgroundLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED;
        //        }
        val activityRecognition = false
        //        if (android.os.Build.VERSION.SDK_INT >= 29) {
        //            activityRecognition = ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED;
        //        }
        val rs = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        val ws = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation || backgroundLocation || rs || ws || activityRecognition
    }

    fun requestPermissions(activity: Activity) {
        /*        if (Build.VERSION.SDK_INT >= 29) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.ACTIVITY_RECOGNITION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    },
                    REQUEST_EXTERNAL_STORAGE
            );
        } else*/ ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                Companion.REQUEST_EXTERNAL_STORAGE
        )
    }

    companion object {
        private const val YOUR_TOKEN = ""  // set your token
        private const val REQUEST_EXTERNAL_STORAGE = 1
    }
}

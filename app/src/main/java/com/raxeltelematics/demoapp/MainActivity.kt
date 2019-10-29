package com.raxeltelematics.demoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.raxeltelematics.v2.sdk.TrackingApi
import com.raxeltelematics.v2.sdk.utils.permissions.PermissionsDialogFragment
import com.raxeltelematics.v2.sdk.utils.permissions.PermissionsWizardActivity
import com.raxeltelematics.v2.sdk.utils.permissions.PermissionsWizardActivity.Companion.WIZARD_RESULT_ALL_GRANTED
import com.raxeltelematics.v2.sdk.utils.permissions.PermissionsWizardActivity.Companion.WIZARD_RESULT_CANCELED
import com.raxeltelematics.v2.sdk.utils.permissions.PermissionsWizardActivity.Companion.WIZARD_RESULT_NOT_ALL_GRANTED
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var permsFragment: PermissionsDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceId.setText(YOUR_TOKEN)
        val trackingApi = TrackingApi.getInstance()

        permsFragment = PermissionsDialogFragment.newInstants(dismissIfAllGranted = false)
        permsFragment?.setPermissionsGrantedListener(object : PermissionsDialogFragment.PermissionsGrantedListener {
            override fun onGrantedStatus(allPermsGranted: Boolean) {
                if (allPermsGranted) enableSDK() // enabled SDK if not enabled
            }
        })

        /**
        In your application you need to request runtime permissions (see list below) for Raxel Pulse SDK to work correctly.

        [android.Manifest.permission.ACCESS_FINE_LOCATION],
        [android.Manifest.permission.ACCESS_COARSE_LOCATION],
        [android.Manifest.permission.WRITE_EXTERNAL_STORAGE],
        [android.Manifest.permission.READ_EXTERNAL_STORAGE]
        [android.Manifest.permission.ACCESS_BACKGROUND_LOCATION] (for Android >=10 (Q))
        [android.Manifest.permission.ACTIVITY_RECOGNITION] (for Android >=10 (Q))

        1. You can start wizard or show dialog before user logged in - for example to request all required permissions for SDK on the App start
        2. You can start wizard after user logged in the App for the first time and had got Device ID (SDK token) when SDK is NOT enabled and after user finished wizard and granted permissions - enable SDK with user's Device ID
        3. If user logged in and SDK is enabled, but revoked access for any of required permissions - you [must] show wizard or dialog to request required permissions when the App started or opened from background.

         */

        if (!trackingApi.isAllRequiredPermissionsAndSensorsGranted() && !trackingApi.isSdkEnabled()) { // if some permissions not granted - start wizard for request permissions
            startActivityForResult(PermissionsWizardActivity.getStartWizardIntent(this, false, false), PermissionsWizardActivity.WIZARD_PERMISSIONS_CODE)
        } else if (!trackingApi.isAllRequiredPermissionsAndSensorsGranted() && trackingApi.isSdkEnabled()) {
            showPermissionsDialog()
        } else {
            Log.d("TAG", "MainActivity isAllRequiredPermissionsAndSensorsGranted true")
            enableSDK()
        }

        startWizard.setOnClickListener {
            if (!trackingApi.isAllRequiredPermissionsAndSensorsGranted()) {
                startActivityForResult(PermissionsWizardActivity.getStartWizardIntent(this, false, false), PermissionsWizardActivity.WIZARD_PERMISSIONS_CODE)
            } else {
                Log.d("TAG", "MainActivity isAllRequiredPermissionsAndSensorsGranted true")
                Toast.makeText(this, "All Required Permissions Granted!", Toast.LENGTH_SHORT).show()
            }
        }

        startPermsDialog.setOnClickListener {
            if (!trackingApi.isAllRequiredPermissionsAndSensorsGranted()) {
                showPermissionsDialog()
            } else {
                Log.d("TAG", "MainActivity isAllRequiredPermissionsAndSensorsGranted true")
                Toast.makeText(this, "All Required Permissions Granted!", Toast.LENGTH_SHORT).show()
            }
        }

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
            if (!trackingApi.isAllRequiredPermissionsGranted()) {
                trackingApi.setEnableSdk(false)
                trackingApi.clearDeviceID()
            }
        }

        start.setOnClickListener {
            trackingApi.startTracking()
        }

        stop.setOnClickListener {
            trackingApi.stopTracking()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionsWizardActivity.WIZARD_PERMISSIONS_CODE) {
            when (resultCode) {
                WIZARD_RESULT_ALL_GRANTED -> {
                    // when user finished wizard with all required permissions granted - enable SDK if Device ID is exist
                    Log.d("TAG", "MainActivity onActivityResult WIZARD_PERMISSIONS_CODE WIZARD_RESULT_ALL_GRANTED")
                    enableSDK()
                }
                WIZARD_RESULT_CANCELED -> {
                    // when user canceled wizard
                    Log.d("TAG", "MainActivity onActivityResult WIZARD_PERMISSIONS_CODE WIZARD_RESULT_CANCELED")
                    Toast.makeText(this, "Wizard canceled!", Toast.LENGTH_SHORT).show()
                }
                WIZARD_RESULT_NOT_ALL_GRANTED -> {
                    // when user not granted all required permissions
                    Log.d("TAG", "MainActivity onActivityResult WIZARD_PERMISSIONS_CODE WIZARD_RESULT_NOT_ALL_GRANTED")
                    Toast.makeText(this, "NOT All Required Permissions Granted!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun enableSDK() {
        if (deviceId.text.toString().isEmpty()) {
            Toast.makeText(this, "Device ID is empty", Toast.LENGTH_LONG).show()
            return
        }
        TrackingApi.getInstance().setDeviceID(deviceId.text.toString())
        TrackingApi.getInstance().setEnableSdk(true)
        Toast.makeText(this, "SDK enabled!", Toast.LENGTH_LONG).show()
    }

    private fun showPermissionsDialog() {
        permsFragment?.let {
            if (!permsFragment!!.isVisible) permsFragment?.show(supportFragmentManager, PermissionsDialogFragment.PERMISSION_FRAGMENT_TAG)
        }
    }

    companion object {
        private const val YOUR_TOKEN = ""  // set your token
    }
}

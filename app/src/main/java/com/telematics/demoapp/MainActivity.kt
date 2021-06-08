package com.telematics.demoapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.raxeltelematics.v2.sdk.TrackingApi
import com.raxeltelematics.v2.sdk.utils.permissions.PermissionsDialogFragment
import com.raxeltelematics.v2.sdk.utils.permissions.PermissionsWizardActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	private val trackingApi = TrackingApi.getInstance()

	@SuppressLint("MissingPermission")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		deviceId.setText(YOUR_TOKEN)

		dashboardButton.setOnClickListener {
			if (trackingApi.isSdkEnabled()) startActivity(
				Intent(
					this,
					DashboardStatisticsActivity::class.java
				)
			)
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
			if (trackingApi.isSdkEnabled()) startActivity(
				Intent(
					this,
					TripsListActivity::class.java
				)
			)
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
			if (deviceId.text.toString().isBlank()) {
				Toast.makeText(this, "Device ID is empty", Toast.LENGTH_LONG).show()
				return@setOnClickListener
			}
			if (!trackingApi.isAllRequiredPermissionsAndSensorsGranted()) {
				Toast.makeText(this, "Please grant all required permissions", Toast.LENGTH_LONG)
					.show()
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

		startPermissionsWizard.setOnClickListener {
			if (!TrackingApi.getInstance().isAllRequiredPermissionsAndSensorsGranted()) {
				startActivityForResult(
					PermissionsWizardActivity.getStartWizardIntent(
						this,
						enableAggressivePermissionsWizard = false,
						enableAggressivePermissionsWizardPage = true
					), PermissionsWizardActivity.WIZARD_PERMISSIONS_CODE
				)
			} else Toast.makeText(this, "All permissions are already granted", Toast.LENGTH_SHORT)
				.show()
		}

		startPermissionsDialog.setOnClickListener {
			if (!TrackingApi.getInstance().isAllRequiredPermissionsAndSensorsGranted()) {
				val permsFragment =
					PermissionsDialogFragment.newInstants(dismissIfAllGranted = true)
				permsFragment.setPermissionsGrantedListener(object :
					PermissionsDialogFragment.PermissionsGrantedListener {
					override fun onGrantedStatus(allPermsGranted: Boolean) {
						Log.d("TAG", "PermissionsDialogFragment onGrantedStatus: $allPermsGranted")
					}
				})
				permsFragment.show(
					supportFragmentManager,
					PermissionsDialogFragment.PERMISSION_FRAGMENT_TAG
				)
			} else Toast.makeText(this, "All permissions are already granted", Toast.LENGTH_SHORT)
				.show()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == PermissionsWizardActivity.WIZARD_PERMISSIONS_CODE) {
			when (resultCode) {
				PermissionsWizardActivity.WIZARD_RESULT_ALL_GRANTED -> {
					Log.d("TAG", "onActivityResult: WIZARD_RESULT_ALL_GRANTED")
					Toast.makeText(this, "All permissions was granted", Toast.LENGTH_SHORT).show()
				}
				PermissionsWizardActivity.WIZARD_RESULT_NOT_ALL_GRANTED -> {
					Log.d("TAG", "onActivityResult: WIZARD_RESULT_NOT_ALL_GRANTED")
					Toast.makeText(this, "All permissions was not granted", Toast.LENGTH_SHORT)
						.show()
				}
				PermissionsWizardActivity.WIZARD_RESULT_CANCELED -> {
					Log.d("TAG", "onActivityResult: WIZARD_RESULT_CANCELED")
					Toast.makeText(this, "Wizard cancelled", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}

	companion object {
		private const val YOUR_TOKEN = ""
	}
}

package com.telematics.demoapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raxeltelematics.v2.sdk.TrackingApi
import com.raxeltelematics.v2.sdk.server.model.Locale
import com.raxeltelematics.v2.sdk.server.model.sdk.Track
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_trips_list.*

class TripsListActivity : AppCompatActivity() {

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
	private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips_list)

        viewManager = LinearLayoutManager(this)

        recycleView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager
        }
        loadData()
    }

    private fun loadData() {
        val disp = Single.fromCallable {
            TrackingApi.getInstance().getTracks(locale = Locale.EN, offset = 0, limit = 10)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    it.printStackTrace()
                }
                .subscribe { data ->
                    updateData(data)
                }
		disposables.add(disp)
    }

    fun updateData(result: Array<Track>?) {
        if (result != null) {
            val viewModels = result.map {
                TrackModel(
                        addressStart = it.addressStart,
                        addressEnd = it.addressEnd,
                        endDate = it.endDate,
                        startDate = it.startDate,
                        trackId = it.trackId,
                        accelerationCount = it.accelerationCount,
                        decelerationCount = it.decelerationCount,
                        distance = it.distance,
                        duration = it.duration,
                        rating = it.rating,
                        phoneUsage = it.phoneUsage,
                        originalCode = it.originalCode,
                        hasOriginChanged = it.hasOriginChanged,
                        midOverSpeedMileage = it.midOverSpeedMileage,
                        highOverSpeedMileage = it.highOverSpeedMileage,
                        drivingTips = it.drivingTips,
                        shareType = it.shareType,
                        cityStart = it.cityStart,
                        cityFinish = it.cityFinish
                )
            }
            val viewAdapter = TrackAdapter(viewModels) {
                showTrackDetails(it)
            }
            recycleView.adapter = viewAdapter
        }
    }

	override fun onDestroy() {
		disposables.dispose()
		super.onDestroy()
	}

    private fun showTrackDetails(it: String) {
        startActivity(Intent(this, TrackDetailsActivity::class.java).putExtra(TrackDetailsActivity.EXTRA_TRACK_ID, it))
    }
}
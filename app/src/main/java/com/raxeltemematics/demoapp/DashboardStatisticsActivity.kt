package com.raxeltemematics.demoapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.raxeltelematics.v2.sdk.TrackingApi
import com.raxeltelematics.v2.sdk.server.model.sdk.DashboardInfo
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardStatisticsActivity : AppCompatActivity() {

    val EXTRA_TRACK_ID = "extra.track.id"
//    lateinit var trackId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

//        trackId = intent!!.extras!!.getString(EXTRA_TRACK_ID)!!
        loadDashboard()

        maneuversStatView.setOnClickListener {
            startActivity(Intent(this, StatisticActivity::class.java)
                    .putExtra(StatisticActivity.EXTRA_STATISTIC_TYPE, StatisticActivity.STATISTIC_TYPE_MANEUVERS))
        }

        speedingStatView.setOnClickListener {
            startActivity(Intent(this, StatisticActivity::class.java)
                    .putExtra(StatisticActivity.EXTRA_STATISTIC_TYPE, StatisticActivity.STATISTIC_TYPE_SPEEDING))
        }

        mileageStatView.setOnClickListener {
            startActivity(Intent(this, StatisticActivity::class.java)
                    .putExtra(StatisticActivity.EXTRA_STATISTIC_TYPE, StatisticActivity.STATISTIC_TYPE_MILEAGE))
        }

        phoneStatView.setOnClickListener {
            startActivity(Intent(this, StatisticActivity::class.java)
                    .putExtra(StatisticActivity.EXTRA_STATISTIC_TYPE, StatisticActivity.STATISTIC_TYPE_PHONE_USAGE))
        }
    }

    private fun loadDashboard() {
        val d = Single.fromCallable {
            TrackingApi.getInstance().getDashboardInfo()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    updateDashboard(result)
                }
    }

    private fun updateDashboard(result: DashboardInfo?) {
        if (result != null) {
            ratingTitleTextView.text = "${result.rating} points out of 100"
            maneuversView.text = "${result.drivingLevel} points out of 100"
            speedingView.text = "${result.speedLevel} points out of 100"
            mileageView.text = "${result.mileageLevel} points out of 100"
            phoneView.text = "${result.phoneLevel} points out of 100"
        }
    }
}
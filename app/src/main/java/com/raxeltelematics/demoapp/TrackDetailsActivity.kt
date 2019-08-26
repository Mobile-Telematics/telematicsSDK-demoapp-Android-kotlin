package com.raxeltelematics.demoapp

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.GeoPolyline
import com.here.android.mpa.common.Image
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import com.raxeltelematics.v2.sdk.TrackingApi
import com.raxeltelematics.v2.sdk.server.model.Locale
import com.raxeltelematics.v2.sdk.server.model.sdk.TrackDetails
import com.raxeltelematics.v2.sdk.server.model.sdk.TrackOriginDictionary
import com.raxeltelematics.v2.sdk.server.model.sdk.TrackPoint
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_track_details_v2.*
import java.io.IOException
import java.util.*

class TrackDetailsActivity : AppCompatActivity() {

    companion object {
        val EXTRA_TRACK_ID = "extra.track.id"
    }

    val arrayOriginTypesStr = arrayOf(
            "OriginalDriver",
            "Passanger",
            "Bus",
            "Taxi",
            "Train",
            "Bicycle",
            "Motorcycle",
            "Walking",
            "Running",
            "Other"
    )

    val arrayOriginDescr = arrayOf(
            R.string.origin_driver,
            R.string.origin_passenger,
            R.string.origin_bus,
            R.string.origin_taxi,
            R.string.origin_train,
            R.string.origin_bicycle,
            R.string.origin_motocycle,
            R.string.origin_walking,
            R.string.origin_run,
            R.string.origin_other
    )

    private var map: Map? = null

    lateinit var trackId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_details_v2)

        val item2 = supportFragmentManager!!.findFragmentById(R.id.trip_view_map) as SupportMapFragment

        item2.init {
            if (it == OnEngineInitListener.Error.NONE) {
                map = item2.map
            }
        }

        trackId = intent!!.extras!!.getString(EXTRA_TRACK_ID)!!

        loadTrackDetails(trackId)
    }

    private fun loadTrackDetails(trackId: String) {
        val disposable = Single.fromCallable {
            TrackingApi.getInstance().getTrackDetails(trackId, Locale.EN)
        }.doOnError {
            it.printStackTrace()
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { details ->
                    updateDetailsView(details)
                    val points = details.points
                    if (points != null) {
                        initMap(points.map { convert(it) })
                    }
                }
    }

    fun convert(tripPoint: TrackPoint): TripPointModel {
        fun getSpeedColor(type: String?): Int {
            return when (type) {
                "norm" -> R.color.colorSpeedTypeNormal
                "mid" -> R.color.colorSpeedTypeMid
                "high" -> R.color.colorSpeedTypeHigh
                else -> R.color.colorSpeedTypeNormal
            }
        }

        fun getAlertImageId(type: String?): Int {
            return when (type) {
                "acc" -> R.drawable.ic_dot_rapid_acc
                "deacc" -> R.drawable.ic_dot_harsh_braking
                else -> 0
            }
        }

        return TripPointModel(
                latitude = tripPoint.latitude,
                longitude = tripPoint.longitude,
                speedColor = getSpeedColor(tripPoint.speedType),
                alertTypeImage = getAlertImageId(tripPoint.alertType),
                usePhone = tripPoint.phoneUsage
        )
    }

    private fun updateDetailsView(details: TrackDetails?) {
        if (details != null) {
//            trip_view_map.
            originButton.text = getString(arrayOriginDescr[arrayOriginTypesStr.indexOf(details.originalCode!!)])
            originButton.isEnabled = !details.hasOriginChanged
            if (!details.hasOriginChanged) {
                originButton.setOnClickListener {
                    originButton.isEnabled = false
                    loadDict(originButton)
                }
            }
            totalRating.text = "${details.rating.toInt()}/5"
            distance.text = "${String.format("%.1f", details.distance)} km"
            timeInTrip.text = String.format("%.1f mins", details.duration)

            startAddress.text = "${details.addressStart}"
            stopAddress.text = "${details.addressEnd}"
            startTime.text = "${details.startDate}"
            stopTime.text = "${details.endDate}"
            accelCount.text = "times: ${details.accelerationCount}"
            speedsCount.text = String.format("%.1f km", (details.highOverSpeedMileage + details.midOverSpeedMileage))
            breaksCount.text = "times: ${details.decelerationCount}"
            phoneCount.text = "${details.phoneUsage.toInt()} mins"
        }
    }

    private fun loadDict(originButton: Button) {
        val disposable = Single.fromCallable {
            TrackingApi.getInstance().getTrackOriginDict(Locale.EN)
        }.doOnError {
            it.printStackTrace()
            originButton.isEnabled = true
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { dict ->
                    originButton.isEnabled = true

                    val items = dict.map { it.name!! }.toTypedArray()
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("Change origin to")
                    dialog.setSingleChoiceItems(items, 0) { d, which ->
                        d.dismiss()
                        changeOriginTo(dict[which])
                    }
                    dialog.show()
                }
    }

    private fun changeOriginTo(origin: TrackOriginDictionary) {
        val disposable = Single.fromCallable {
            TrackingApi.getInstance().changeTrackOrigin(trackId, origin.code!!)
        }.doOnError {
            it.printStackTrace()
            originButton.isEnabled = true
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { _ ->
                    loadTrackDetails(trackId)
                }
    }

    private fun initMap(tripPoints: List<TripPointModel>) {

        fun getImage(resId: Int): Image {
            val image = Image()
            try {
                image.setImageResource(resId)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            return image
        }

        var listCoordinates: MutableList<GeoCoordinate> = ArrayList()
        val listLines = ArrayList<MapPolyline>()
        val listMarkers = ArrayList<MapMarker>()
        val imageStart = getImage(R.drawable.marker_a_)
        val imageStop = getImage(R.drawable.marker_b_)

        for (i in tripPoints.indices) {
            val point = tripPoints[i]

            val latitude = point.latitude
            val longitude = point.longitude
            val geo = GeoCoordinate(latitude, longitude, 0.0)
            if (i > 0) {
                listCoordinates.add(geo)
                addLine(listCoordinates, listLines, point)
                listCoordinates = ArrayList()
                listCoordinates.add(geo)

            } else if (i == 0) {
                addMarker(listMarkers, imageStart, geo)
                listCoordinates.add(geo)
            }

            if (i == tripPoints.size - 1) {
                addMarker(listMarkers, imageStop, geo)
            }

            if (point.alertTypeImage != 0) {
                addMarker(listMarkers, getImage(point.alertTypeImage), geo)
            }
        }

        if (listCoordinates.size > 1) {
            addLine(listCoordinates, listLines, tripPoints[tripPoints.size - 1])
        }

        val listMapObjects = ArrayList<MapObject>()
        for (line in listLines) {
            listMapObjects.add(line)
        }
        if (listMarkers.size > 0) {
            for (marker in listMarkers) {
                listMapObjects.add(marker)
            }
        }

        updateMarkers(tripPoints, listMapObjects)
    }

    private fun addLine(
            listCoordinates: List<GeoCoordinate>,
            listLines: MutableList<MapPolyline>,
            point: TripPointModel
    ) {
        if (point.usePhone) {
            val line2 = MapPolyline(GeoPolyline(listCoordinates))
            line2.lineColor = ContextCompat.getColor(this, R.color.trips_phone_line_color)
            line2.lineWidth = 25
            listLines.add(line2)
        }
        val line = MapPolyline(GeoPolyline(listCoordinates))
        val speedColor = point.speedColor
        line.lineColor = ContextCompat.getColor(this, speedColor)
        line.lineWidth = 15
        listLines.add(line)
    }

    private fun addMarker(listMarkers: MutableList<MapMarker>, imageStop: Image, geo: GeoCoordinate) {
        val marker = MapMarker()
        marker.coordinate = geo
        marker.icon = imageStop
        listMarkers.add(marker)
    }

    private fun updateMarkers(tripPoints: List<TripPointModel>, listMapObjects: List<MapObject>) {
        if (map != null) {
            map!!.removeMapObjects(listMapObjects)
            map!!.addMapObjects(listMapObjects)
            if (tripPoints.isNotEmpty()) {
                val tripPointModel = tripPoints[0]
                var latitude = tripPointModel.latitude
                latitude -= 0.02
                map!!.setCenter(
                        GeoCoordinate(
                                latitude, tripPointModel
                                .longitude, 5.0
                        ), Map.Animation.NONE
                )
                map!!.setUseSystemLanguage()
            }
        }
    }
}
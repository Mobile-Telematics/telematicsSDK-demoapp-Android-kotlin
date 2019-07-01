package com.example.testandroidgitflow

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.raxeltelematics.v2.sdk.TrackingApi
import com.raxeltelematics.v2.sdk.server.model.StatisticPeriod
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_statistic.*
import java.text.SimpleDateFormat
import java.util.*

class StatisticActivity : AppCompatActivity() {

    companion object {
        val EXTRA_STATISTIC_TYPE = "extra.statistic.type"
        val STATISTIC_TYPE_MANEUVERS = "statistic.type.maneuvers"
        val STATISTIC_TYPE_DRIVING_TIME = "statistic.type.driving.time"
        val STATISTIC_TYPE_SPEEDING = "statistic.type.speeding"
        val STATISTIC_TYPE_MILEAGE = "statistic.type.mileage"
        val STATISTIC_TYPE_PHONE_USAGE = "statistic.type.phone.usage"
    }

    lateinit var type: String
    var typeInt: Int =  StatisticInfoModel.TYPE_MANEUVERS
    lateinit var formatter: StatisticInfoTextFormatterImpl

    var modelObj: StatisticInfoModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        formatter = StatisticInfoTextFormatterImpl(this)

        type = intent!!.extras!!.getString(EXTRA_STATISTIC_TYPE)!!
        typeInt = when (type) {
            STATISTIC_TYPE_MANEUVERS -> StatisticInfoModel.TYPE_MANEUVERS
            STATISTIC_TYPE_DRIVING_TIME -> StatisticInfoModel.TYPE_DRIVING_TIME
            STATISTIC_TYPE_PHONE_USAGE -> StatisticInfoModel.TYPE_PHONE_USAGE
            STATISTIC_TYPE_SPEEDING -> StatisticInfoModel.TYPE_SPEEDING
            STATISTIC_TYPE_MILEAGE -> StatisticInfoModel.TYPE_MILEAGE
            else -> {
                throw IllegalArgumentException("no such type $type")
            }
        }

        updateDStatisticTitles()
        initChart()
        loadStatistic()

        periodGroup.setOnCheckedChangeListener { _, _ ->
            loadStatistic()
        }
    }

    private fun updateDStatisticTitles() {
        title1View.text = getString(formatter.formatTopLeftTitle(typeInt))
        title2View.text = getString(formatter.formatTopRightTitle(typeInt))
        title3View.text = getString(formatter.formatBottomLeftTitle(typeInt))
        title4View.text = getString(formatter.formatBottomRightTitle(typeInt))
    }

    private fun loadStatistic() {

        val period = when (periodGroup.checkedRadioButtonId) {
            R.id.day -> StatisticPeriod.DAY
            R.id.week -> StatisticPeriod.WEEK
            R.id.alltime -> StatisticPeriod.ALL_TIME
            else -> {
                throw IllegalArgumentException("No such period with button id=${periodGroup.checkedRadioButtonId}")
            }
        }

        val d = Single.fromCallable {
            when (type) {
                STATISTIC_TYPE_MANEUVERS -> {
                    val statistic = TrackingApi.getInstance().getDrivingDetailsStatistics(period)
                    StatisticDetailInfoMapper.convert(statistic)
                }
                STATISTIC_TYPE_SPEEDING -> {
                    val statistic = TrackingApi.getInstance().getSpeedDetailStatistics(period)
                    StatisticDetailInfoMapper.convert(statistic)
                }
                STATISTIC_TYPE_MILEAGE -> {
                    val statistic = TrackingApi.getInstance().getMileageDetailsStatistics(period)
                    StatisticDetailInfoMapper.convert(statistic)
                }
                STATISTIC_TYPE_PHONE_USAGE -> {
                    val statistic = TrackingApi.getInstance().getPhoneDetailStatistics(period)
                    StatisticDetailInfoMapper.convert(statistic)
                }
                STATISTIC_TYPE_DRIVING_TIME -> {
                    val statistic = TrackingApi.getInstance().getPhoneDetailStatistics(period)
                    StatisticDetailInfoMapper.convert(statistic)
                }
                else -> {
                    throw Exception("no such type ${type}")
                }
            }
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { model ->
                    this.modelObj = model
                    initGraph(model)
                }
    }

    private fun initChart() {
        lineChart.isDragEnabled = false
        lineChart.setScaleEnabled(false)
        val desc = Description()
        desc.text = ""
        lineChart.description = desc
        lineChart.setDrawGridBackground(false)
        lineChart.legend.isEnabled = false
        lineChart.setDrawMarkers(true)
        lineChart.marker = MyChartMarkerView(this, R.layout.chart_hightlight)
        lineChart.setNoDataText("No chart data")

        /* Horizontal */
        val xAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(true)
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                val model = modelObj
                if (model != null) {
                    Log.d("TAG", "model not null=$model")
                }
                if (model?.grafhPoints1 != null && model.grafhPoints1.isNotEmpty()) {

                    if (model.grafhPoints1.size > value) {
                        val date = model.getGrafhPoints1()[value.toInt()].data as Date?
                        Log.d("TAG", "date not null=$date")
                        return if (date != null) SimpleDateFormat("dd MMM", Locale.getDefault()).format(date) else ""

                    } else {
                        return ""
                    }
                }
                Log.d("TAG", "return null")
                return ""
            }

            override fun getDecimalDigits(): Int {
                return 0
            }
        }


        /* Vertical Left */
        val axisLeft = lineChart.axisLeft
        axisLeft.setDrawGridLines(true)
        axisLeft.setDrawLabels(true)
        axisLeft.setDrawAxisLine(true)
        axisLeft.axisMinimum = 0.0f

        /* Vertical Right */
        val axisRight = lineChart.axisRight
        axisRight.setDrawGridLines(true)
        axisRight.setDrawLabels(true)
        axisRight.setDrawAxisLine(true)
        axisRight.axisMinimum = 0.0f

        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val dataSets = lineChart.lineData.dataSets
                val x = e.x
                val iLineDataSet = dataSets[0]
                val entryForXPos = iLineDataSet.getEntryForIndex(x.toInt())
                val y = entryForXPos.y
                var y2: Float
                var y3: Float

                val values = ArrayList<Highlight>()
                values.add(Highlight(x, y, 0))

                if (2 <= dataSets.size) {
                    val entryForXPos1 = dataSets[1].getEntryForIndex(x.toInt())
                    y2 = entryForXPos1.y
                    values.add(Highlight(x, y2, 1))
                }
                if (3 <= dataSets.size) {
                    val entryForXPos2 = dataSets[2].getEntryForIndex(x.toInt())
                    y3 = entryForXPos2.y
                    values.add(Highlight(x, y3, 2))
                }

                lineChart.highlightValues(values.toTypedArray())
            }

            override fun onNothingSelected() {
            }
        })
    }

    private inner class MyChartMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

        override fun getOffset(): MPPointF {
            return MPPointF((-width / 2).toFloat(), (-height / 2).toFloat())
        }
    }

    protected fun initGraph(infoModel: StatisticInfoModel) {
        val graph1 = infoModel.grafhPoints1
        var dataSet: LineDataSet? = null
        if (graph1 != null && graph1.size != 0) {
            dataSet = getLineDataSet(graph1, ContextCompat.getColor(lineChart.context, R.color.graphColor1))
        }

        val graph2 = infoModel.grafhPoints2
        var dataSet2: LineDataSet? = null
        if (graph2 != null && graph2.size != 0) {
            dataSet2 = getLineDataSet(graph2, ContextCompat.getColor(lineChart.context, R.color.graphColor2))
        }

        val graph3 = infoModel.grafhPoints3
        var dataSet3: LineDataSet? = null
        if (graph3 != null && graph3.size != 0) {
            dataSet3 = getLineDataSet(graph3, ContextCompat.getColor(lineChart.context, R.color.graphColor3)) // change color!
        }

        if (dataSet != null) {

            val lineData = LineData()
            lineData.addDataSet(dataSet)

            if (dataSet2 != null) {
                lineData.addDataSet(dataSet2)
            }
            if (dataSet3 != null) {
                lineData.addDataSet(dataSet3)
            }


            lineData.setDrawValues(false)

            lineChart.data = lineData

            initLabelAx()

            lineChart.invalidate()

        } else {
            labelAx.setVisibility(View.INVISIBLE)
            lineChart.clear()
        }
    }

    protected fun getLineDataSet(graph1: List<Entry>, blue: Int): LineDataSet {
        val dataSet = LineDataSet(graph1, "") // add entries to dataset
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSet.color = blue
        //			dataSet.setValueTextColor(Color.BLUE);
        dataSet.setDrawFilled(true)
        dataSet.fillColor = blue
        dataSet.setFillFormatter { dataSet1, dataProvider -> -999990f }
        dataSet.setDrawCircles(false)
        dataSet.setDrawCircleHole(false)
        dataSet.cubicIntensity = 0.2f
        dataSet.fillAlpha = 200

        dataSet.isHighlightEnabled = true // allow highlighting for DataSet

        // set this to false to disable the drawing of highlight indicator (lines)
        dataSet.setDrawHighlightIndicators(true)
        dataSet.highLightColor = Color.BLACK
        dataSet.setDrawHorizontalHighlightIndicator(true)
        dataSet.setDrawVerticalHighlightIndicator(true)
        return dataSet
    }


    private fun initLabelAx() {
        labelAx.visibility = View.VISIBLE

        when (type) {
            STATISTIC_TYPE_MANEUVERS -> {
                labelAx.setText(R.string.main_stat_times_label)
            }

            STATISTIC_TYPE_MILEAGE -> {
                labelAx.setText(R.string.main_stat_km_label)
            }

            STATISTIC_TYPE_SPEEDING -> {
                labelAx.setText(R.string.main_stat_km_label)
            }

            STATISTIC_TYPE_PHONE_USAGE -> {
                labelAx.setText(R.string.main_stat_min_label)
            }

            STATISTIC_TYPE_DRIVING_TIME -> {
                labelAx.setText(R.string.main_stat_h_label)
            }
            else -> {
                labelAx.setText(R.string.main_stat_unknown_label)
            }
        }
    }
}
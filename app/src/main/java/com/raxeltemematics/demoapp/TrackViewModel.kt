package com.raxeltemematics.demoapp

class TrackViewModel(
    var addressStart: String?,
    var addressEnd: String?,
    var endDate: String?,
    var startDate: String?,
    var trackId: String?,
    var accelerationCount: Int = 0,
    var decelerationCount: Int = 0,
    var distance: Double = 0.toDouble(),
    var duration: Double = 0.toDouble(),
    var rating: Double = 0.toDouble(),
    var phoneUsage: Double = 0.toDouble(),
    var originalCode: String?,
    var hasOriginChanged: Boolean = false,
    var midOverSpeedMileage: Double = 0.toDouble(),
    var highOverSpeedMileage: Double = 0.toDouble(),
    var drivingTips: String?,
    var shareType: String?,
    var cityStart: String?,
    var cityFinish: String?
)
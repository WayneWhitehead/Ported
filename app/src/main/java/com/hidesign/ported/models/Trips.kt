package com.hidesign.ported.models

import com.tomtom.online.sdk.common.location.LatLng

class Trips {
    lateinit var startAddress: String
    lateinit var endAddress: String
    var tripDate: Long = 0
    private lateinit var startLocation: LatLng
    private lateinit var endLocation: LatLng
    var tripDistance = 0f

    constructor() {}
    constructor(startLocation: LatLng, startAddress: String, endLocation: LatLng, endAddress: String, tripDate: Long, tripDistance: Float) {
        this.startAddress = startAddress
        this.endAddress = endAddress
        this.tripDate = tripDate
        this.startLocation = startLocation
        this.endLocation = endLocation
        this.tripDistance = tripDistance
    }

}
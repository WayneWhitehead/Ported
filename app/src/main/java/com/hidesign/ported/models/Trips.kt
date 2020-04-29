package com.hidesign.ported.models

import com.tomtom.online.sdk.common.location.LatLng

class Trips {
    var startAddress: String? = null
    var endAddress: String? = null
    var tripDate: Long = 0
    var startLocation: LatLng? = null
    var endLocation: LatLng? = null
    var tripDistance = 0f

    constructor() {}
    constructor(startLocation: LatLng?, startAddress: String?, endLocation: LatLng?, endAddress: String?, tripDate: Long, tripDistance: Float) {
        this.startAddress = startAddress
        this.endAddress = endAddress
        this.tripDate = tripDate
        this.startLocation = startLocation
        this.endLocation = endLocation
        this.tripDistance = tripDistance
    }

}
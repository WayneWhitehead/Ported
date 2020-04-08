package com.hidesign.ported.models;

import com.tomtom.online.sdk.common.location.LatLng;

public class Trips {

    private String startAddress, endAddress;
    private long tripDate;
    private LatLng startLocation, endLocation;
    private float tripDistance;

    public Trips(){}

    public Trips(LatLng startLocation, String startAddress, LatLng endLocation, String endAddress, long tripDate, float tripDistance) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.tripDate = tripDate;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.tripDistance = tripDistance;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public long getTripDate() {
        return tripDate;
    }

    public void setTripDate(long tripDate) {
        this.tripDate = tripDate;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public float getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(float tripDistance) {
        this.tripDistance = tripDistance;
    }
}

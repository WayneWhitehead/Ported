package com.hidesign.ported

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.hidesign.ported.models.Trips
import com.tomtom.online.sdk.routing.data.TravelMode
import timber.log.Timber
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class Functions {

    fun sortList(list: MutableList<Trips>) {
        val n = list.size
        var k: Int
        for (m in n downTo 0) {
            for (i in 0 until n - 1) {
                k = i + 1
                if (list[i].tripDate < list[k].tripDate) {
                    val temp: Trips = list[i]
                    list[i] = list[k]
                    list[k] = temp
                }
            }
        }
    }

    fun getTravelMode(i: Int): TravelMode {
        when (i) {
            2131230803 -> return TravelMode.CAR
            2131230804 -> return TravelMode.BUS
            2131230805 -> return TravelMode.PEDESTRIAN
        }
        return TravelMode.CAR
    }

    fun getAddress(latitude: Double, longitude: Double, c: Context?): String {
        val geo = Geocoder(c, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geo.getFromLocation(latitude, longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
            Timber.e("Error getting Street Address: ")
        }
        assert(addresses != null)
        return addresses!![0].getAddressLine(0)
    }

    fun getDate(milliSeconds: Long, dateFormat: String): String {
        val simple: DateFormat = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        val result = Date(milliSeconds)
        return simple.format(result)
    }

    fun calculateDistance(d: Float): List<String> {
        var distance = d
        val temp: MutableList<String> = ArrayList()
        return if (distance >= 1000) {
            distance /= 1000f
            temp.add(String.format(Locale.ENGLISH, "%.1f", distance))
            temp.add("Km")
            temp
        } else {
            temp.add(String.format(Locale.ENGLISH, "%.0f", distance))
            temp.add("m")
            temp
        }
    }

    fun distanceToNextTurn(d: Float): String {
        var distance = d
        return if (distance >= 1000) {
            distance /= 1000f
            String.format(Locale.ENGLISH, "%.1f", distance) + "Km"
        } else if (distance >= 950 && distance < 1000) {
            950.toString() + "m"
        } else if (distance >= 900 && distance < 950) {
            900.toString() + "m"
        } else if (distance >= 850 && distance < 900) {
            850.toString() + "m"
        } else if (distance >= 800 && distance < 850) {
            800.toString() + "m"
        } else if (distance >= 750 && distance < 800) {
            750.toString() + "m"
        } else if (distance >= 700 && distance < 750) {
            700.toString() + "m"
        } else if (distance >= 650 && distance < 700) {
            650.toString() + "m"
        } else if (distance >= 600 && distance < 650) {
            600.toString() + "m"
        } else if (distance >= 550 && distance < 600) {
            550.toString() + "m"
        } else if (distance >= 500 && distance < 550) {
            500.toString() + "m"
        } else if (distance >= 450 && distance < 500) {
            450.toString() + "m"
        } else if (distance >= 400 && distance < 450) {
            400.toString() + "m"
        } else if (distance >= 350 && distance < 400) {
            350.toString() + "m"
        } else if (distance >= 300 && distance < 350) {
            300.toString() + "m"
        } else if (distance >= 250 && distance < 300) {
            250.toString() + "m"
        } else if (distance >= 200 && distance < 250) {
            200.toString() + "m"
        } else if (distance >= 150 && distance < 200) {
            150.toString() + "m"
        } else if (distance >= 100 && distance < 150) {
            100.toString() + "m"
        } else if (distance >= 50 && distance < 100) {
            50.toString() + "m"
        } else ""
    }

    fun formatTimeFromSeconds(s: Long): String {
        var secondsTotal = s
        val hours = TimeUnit.SECONDS.toHours(secondsTotal)
        val minutes = TimeUnit.SECONDS.toMinutes(secondsTotal) - TimeUnit.HOURS.toMinutes(hours)
        var timeFormat = ""
        if (hours != 0L) {
            timeFormat = "H'h' m'min'"
        } else {
            if (minutes != 0L) {
                timeFormat = "m"
            }
        }
        secondsTotal = abs(secondsTotal)
        return android.text.format.DateFormat.format(timeFormat, TimeUnit.SECONDS.toMillis(secondsTotal)) as String
    }
}
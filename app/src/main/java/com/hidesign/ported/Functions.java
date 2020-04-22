package com.hidesign.ported;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.hidesign.ported.models.Trips;
import com.tomtom.online.sdk.routing.data.TravelMode;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class Functions {

    public Functions(){}

    public void sortList(List<Trips> list){
        int n = list.size();
        int k;
        for (int m = n; m >= 0; m--) {
            for (int i = 0; i < n - 1; i++) {
                k = i + 1;
                if (list.get(i).getTripDate() < list.get(k).getTripDate()) {
                    Trips temp;
                    temp = list.get(i);
                    list.set(i, list.get(k));
                    list.set(k, temp);
                }
            }
        }
    }

    public TravelMode getTravelMode(int i){
        switch (i) {
            case 2131230803: return TravelMode.CAR;
            case 2131230804: return TravelMode.BUS;
            case 2131230805: return TravelMode.PEDESTRIAN;
        }
        return TravelMode.CAR;
    }

    public String getAddress(double latitude, double longitude, Context c) {
        Geocoder geo = new Geocoder(c, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geo.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e("Error getting Street Address: ");
        }

        assert addresses != null;
        return addresses.get(0).getAddressLine(0);
    }

    public String getDate(long milliSeconds, String dateFormat) {
        DateFormat simple = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        Date result = new Date(milliSeconds);
        return simple.format(result);
    }

    public List<String> calculateDistance(float distance){
        List<String> temp = new ArrayList<>();
        if (distance >= 1000){
            distance/=1000;
            temp.add(String.format(Locale.ENGLISH, "%.1f", distance)) ;
            temp.add("Km");
            return temp;
        } else {
            temp.add(String.format(Locale.ENGLISH, "%.0f", distance));
            temp.add("m");
            return temp;
        }
    }

    public String distanceToNextTurn(float distance){
        if (distance >= 1000){
            distance/=1000;
            return (String.format(Locale.ENGLISH, "%.1f", distance) + "Km") ;
        } else if (distance >= 950 && distance < 1000){
            return 950 + "m";
        } else if (distance >= 900 && distance < 950){
            return 900 + "m";
        } else if (distance >= 850 && distance < 900){
            return 850 + "m";
        } else if (distance >= 800 && distance < 850){
            return 800 + "m";
        } else if (distance >= 750 && distance < 800){
            return 750 + "m";
        } else if (distance >= 700 && distance < 750){
            return 700 + "m";
        } else if (distance >= 650 && distance < 700){
            return 650 + "m";
        } else if (distance >= 600 && distance < 650){
            return 600 + "m";
        } else if (distance >= 550 && distance < 600){
            return 550 + "m";
        } else if (distance >= 500 && distance < 550){
            return 500 + "m";
        } else if (distance >= 450 && distance < 500){
            return 450 + "m";
        } else if (distance >= 400 && distance < 450){
            return 400 + "m";
        } else if (distance >= 350 && distance < 400){
            return 350 + "m";
        } else if (distance >= 300 && distance < 350){
            return 300 + "m";
        } else if (distance >= 250 && distance < 300){
            return 250 + "m";
        } else if (distance >= 200 && distance < 250){
            return 200 + "m";
        } else if (distance >= 150 && distance < 200){
            return 150 + "m";
        } else if (distance >= 100 && distance < 150){
            return 100 + "m";
        } else if (distance >= 50 && distance < 100){
            return 50 + "m";
        } else return "";
    }

    public String formatTimeFromSeconds(long secondsTotal) {
        final String TIME_FORMAT_HOURS_MINUTES = "H'h' m'min'";
        final String TIME_FORMAT_MINUTES = "m";

        long hours = TimeUnit.SECONDS.toHours(secondsTotal);
        long minutes = TimeUnit.SECONDS.toMinutes(secondsTotal) - TimeUnit.HOURS.toMinutes(hours);
        String timeFormat = "";

        if (hours != 0) {
            timeFormat = TIME_FORMAT_HOURS_MINUTES;
        } else {
            if (minutes != 0) {
                timeFormat = TIME_FORMAT_MINUTES;
            }
        }
        secondsTotal = Math.abs(secondsTotal);
        return (String) android.text.format.DateFormat.format(timeFormat, TimeUnit.SECONDS.toMillis(secondsTotal));
    }
}

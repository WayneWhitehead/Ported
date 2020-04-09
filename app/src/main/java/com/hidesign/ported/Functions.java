package com.hidesign.ported;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hidesign.ported.models.Trips;
import com.hidesign.ported.ui.home.PastTripFragment;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.TravelMode;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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

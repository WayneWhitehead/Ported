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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

public class FirebaseFunctions {

    public FirebaseFunctions (){}

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

    public String calculateDistance(float distance){
        String temp;
        if (distance >= 1000){
            distance/=1000;
            temp = String.format(Locale.ENGLISH, "%.2f", distance) + "Km";
            return temp;
        } else {
            temp = String.format(Locale.ENGLISH, "%.0f", distance) + "m";
            return temp;
        }
    }
}

package com.hidesign.ported.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hidesign.ported.Functions;
import com.hidesign.ported.R;
import com.hidesign.ported.adapters.RecyclerAdapter;
import com.hidesign.ported.models.Trips;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PastTripFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView mRecyclerView;
    private List<Trips> pastTrips = new ArrayList<>();
    private Functions fbFunctions = new Functions();
    private DatabaseReference mDatabase;
    private ProgressBar mProgressBar;

    public PastTripFragment(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewInflate = inflater.inflate(R.layout.fragment_list_swipe, container, false);

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();

        mProgressBar = viewInflate.findViewById(R.id.progress_bar);

        mRecyclerView = viewInflate.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child(currentUser.getUid());
        }

        return viewInflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase.child("Trips").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Trips post = postSnapshot.getValue(Trips.class);
                    pastTrips.add(post);
                }
                showList(pastTrips);
                mProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showList(List<Trips> display) {
        Timber.tag(TAG).e("showList: %s", pastTrips.size());

        RecyclerAdapter mAdapter = new RecyclerAdapter(display.size(), R.layout.past_trip_entry);
        mAdapter.setOnRecyclerAdapterListener((adapter, v, position) -> {

            TextView _Date = v.view.findViewById(R.id.Date);
            _Date.setText(fbFunctions.getDate(display.get(position).getTripDate(), "dd MMM yyyy HH:mm"));

            TextView _StartAddress = v.view.findViewById(R.id.StartLocation);
            _StartAddress.setText((display.get(position).getStartAddress()));

            TextView _EndAddress = v.view.findViewById(R.id.EndLocation);
            _EndAddress.setText(display.get(position).getEndAddress());

            List<String> distance = fbFunctions.calculateDistance(display.get(position).getTripDistance());
            TextView _Distance = v.view.findViewById(R.id.Distance);
            _Distance.setText(distance.get(0));
            TextView _DistanceSystem = v.view.findViewById(R.id.DistanceSystem);
            _DistanceSystem.setText(distance.get(1));

            v.view.setOnClickListener(v1 -> {

            });
        });
        mRecyclerView.setAdapter(mAdapter);
    }
}

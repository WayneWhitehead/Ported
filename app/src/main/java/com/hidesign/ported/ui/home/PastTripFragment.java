package com.hidesign.ported.ui.home;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.Visibility;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hidesign.ported.FirebaseFunctions;
import com.hidesign.ported.R;
import com.hidesign.ported.adapters.RecyclerAdapter;
import com.hidesign.ported.models.Trips;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class PastTripFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView mRecyclerView;
    private List<Trips> pastTrips = new ArrayList<>();
    private FirebaseFunctions fbFunctions = new FirebaseFunctions();
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
            _Date.setText(fbFunctions.getDate(display.get(position).getTripDate(), "dd MMM yyyy HH:mm:ss:SSS Z"));

            TextView _StartAddress = v.view.findViewById(R.id.StartLocation);
            _StartAddress.setText((display.get(position).getStartAddress()));

            TextView _EndAddress = v.view.findViewById(R.id.EndLocation);
            _EndAddress.setText(display.get(position).getEndAddress());

            TextView _Distance = v.view.findViewById(R.id.Distance);
            _Distance.setText(fbFunctions.calculateDistance(display.get(position).getTripDistance()));
            TextView _DistanceSystem = v.view.findViewById(R.id.DistanceSystem);
            _DistanceSystem.setText(new String("Km"));

//            v.view.setOnClickListener(v1 -> {
//                Intent i = new Intent(getActivity(), DetailActivity.class);
//                postDetails = display.get(position);
//                i.putExtra("Event", postDetails.getEvent_name());
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v1, "cardTransition" );
//                Objects.requireNonNull(getActivity()).startActivity(i, options.toBundle());
//            });
        });
        mRecyclerView.setAdapter(mAdapter);
    }
}

package com.hidesign.ported.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hidesign.ported.Functions;
import com.hidesign.ported.R;
import com.hidesign.ported.models.Trips;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.location.LatLngAcc;
import com.tomtom.online.sdk.common.permission.AndroidPermissionChecker;
import com.tomtom.online.sdk.common.permission.PermissionChecker;
import com.tomtom.online.sdk.map.Chevron;
import com.tomtom.online.sdk.map.ChevronBuilder;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapConstants;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.driving.LatLngTraceMatchingDataProvider;
import com.tomtom.online.sdk.map.driving.Matcher;
import com.tomtom.online.sdk.map.driving.MatcherFactory;
import com.tomtom.online.sdk.map.model.MapTilesType;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.Instruction;
import com.tomtom.online.sdk.routing.data.InstructionsType;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.TravelMode;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private Boolean requestingLocationUpdates = false;
    private Location uLocation;
    private LatLng latLngCurrentPosition, latLngDestination;
    private LocationCallback locationCallback;

    private TomtomMap tomtomMap;
    private Matcher matcher;
    private Chevron chevron;
    private SearchApi searchApi;

    private Functions func = new Functions();

    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;

    private BottomSheetDialog bottomSheetDialog;
    private View vBottomSheet;
    private AutoCompleteTextView _OriginLocationSearch, _DestinationLocationSearch;
    private MaterialButtonToggleGroup modeOfTransport;

    private final Handler searchTimerHandler = new Handler();
    private Runnable searchRunnable;
    private ArrayAdapter<String> searchAdapter;
    private List<String> searchAutocompleteList = new ArrayList<>();
    private Map<String, LatLng> searchResultsMap = new HashMap<>();

    private final OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NotNull TomtomMap map) {
            tomtomMap = map;
            tomtomMap.setMyLocationEnabled(true);
            tomtomMap.getUiSettings().setMapTilesType(MapTilesType.VECTOR);
            tomtomMap.getUiSettings().getCompassView().hide();
            tomtomMap.getTrafficSettings().turnOnVectorTrafficFlowTiles();
            tomtomMap.getTrafficSettings().turnOnVectorTrafficIncidents();

            tomtomMap.addOnMapLongClickListener(latLng -> newMarker(latLng));
            tomtomMap.addOnMarkerClickListener(marker -> setRoutePlanner(marker.getPosition()));

            getLastKnownLocation();
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity());
        mFirebaseAnalytics.setCurrentScreen(requireActivity(), "Home Fragment", "MapView");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child(mAuth.getCurrentUser().getUid());
        }

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();
        ImageView openDrawer = root.findViewById(R.id.drawerButton);
        DrawerLayout navDrawer = requireActivity().findViewById(R.id.drawer_layout);
        openDrawer.setOnClickListener(v -> navDrawer.openDrawer(GravityCompat.START));

        initLocationSource();

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getAsyncMap(onMapReadyCallback);
        }

        searchApi = OnlineSearchApi.create(requireActivity());
        _OriginLocationSearch = root.findViewById(R.id.atv_main_departure_location);
        _DestinationLocationSearch = root.findViewById(R.id.atv_main_destination_location);
        searchAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_dropdown_item_1line, searchAutocompleteList);
        setTextWatcherToAutoCompleteField(_OriginLocationSearch);
        setTextWatcherToAutoCompleteField(_DestinationLocationSearch);

        return root;
    }

    private void setTextWatcherToAutoCompleteField(final AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setAdapter(searchAdapter);
        autoCompleteTextView.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTimerHandler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 0) {
                    if (s.length() >= 4) {
                        searchRunnable = () -> searchAddress(s.toString());
                        searchAdapter.clear();
                        searchTimerHandler.postDelayed(searchRunnable, 800);
                    }
                }
            }
        });
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            if (autoCompleteTextView == _DestinationLocationSearch) {
                latLngDestination = searchResultsMap.get(item);
                newMarker(latLngDestination);
            }
        });
    }

    private void searchAddress(final String searchWord) {
        searchApi.search(new FuzzySearchQueryBuilder(searchWord).withTypeAhead(true).withMinFuzzyLevel(2)
                .withPreciseness(new LatLngAcc(latLngCurrentPosition, 300000)).build())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<FuzzySearchResponse>() {
                    @Override
                    public void onSuccess(FuzzySearchResponse fuzzySearchResponse) {
                        if (!fuzzySearchResponse.getResults().isEmpty()) {
                            searchAutocompleteList.clear();
                            searchResultsMap.clear();
                            
                            for (FuzzySearchResult result : fuzzySearchResponse.getResults()) {
                                String addressString = result.getAddress().getFreeformAddress();
                                searchAutocompleteList.add(addressString);
                                searchResultsMap.put(addressString, result.getPosition());
                            }
                            searchAdapter.clear();
                            searchAdapter.addAll(searchAutocompleteList);
                            searchAdapter.getFilter().filter("");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("InflateParams")
    private void setRoutePlanner(LatLng marker){
        vBottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_route_planner, null);

        //Checks if the bottom sheet already exists and removes if it does
        if (vBottomSheet.getParent() != null){
            ((ViewGroup) vBottomSheet.getParent()).removeView(vBottomSheet);
        }
        bottomSheetDialog = null;
        bottomSheetDialog = new BottomSheetDialog(requireActivity());
        bottomSheetDialog.setContentView(vBottomSheet);

        if (!bottomSheetDialog.isShowing()){
            uLocation = tomtomMap.getUserLocation();
            Location destination = new Location("");
            destination.setLatitude(marker.getLatitude());
            destination.setLongitude(marker.getLongitude());

            List<String> distanceVar = func.calculateDistance(uLocation.distanceTo(destination));
            TextView distance = vBottomSheet.findViewById(R.id.distance);
            distance.setText(String.format("%s%s", distanceVar.get(0), distanceVar.get(1)));
            TextView address = vBottomSheet.findViewById(R.id.nameAddress);
            address.setText(func.getAddress(marker.getLatitude(), marker.getLongitude(), getContext()));

            modeOfTransport = vBottomSheet.findViewById(R.id.toggleButton);
            modeOfTransport.setSingleSelection(true);

            vBottomSheet.findViewById(R.id.buttonCancel).setOnClickListener(v -> cancel());
            vBottomSheet.findViewById(R.id.getDirections).setOnClickListener(v ->
                    newRoute(new LatLng(uLocation.getLatitude(), uLocation.getLongitude()),
                            marker,
                            func.getTravelMode(modeOfTransport.getCheckedButtonId())));

            bottomSheetDialog.show();
        } else {
            bottomSheetDialog.dismiss();
        }
    }

    @SuppressLint("InflateParams")
    private void setNavigation(FullRoute route){
        vBottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_start_navigation, null);

        //Checks if the bottom sheet already exists and removes if it does
        if (vBottomSheet.getParent() != null){
            ((ViewGroup) vBottomSheet.getParent()).removeView(vBottomSheet);
        }
        bottomSheetDialog = null;
        bottomSheetDialog = new BottomSheetDialog(requireActivity());
        bottomSheetDialog.setContentView(vBottomSheet);

        if (!bottomSheetDialog.isShowing()){
            uLocation = tomtomMap.getUserLocation();

            TextView startAddress = vBottomSheet.findViewById(R.id.startAddress);
            startAddress.setText(func.getAddress(route.getCoordinates().get(0).getLatitude(), route.getCoordinates().get(0).getLongitude(), getContext()));

            TextView endAddress = vBottomSheet.findViewById(R.id.endAddress);
            int lastPoint = route.getCoordinates().size() -1;
            endAddress.setText(func.getAddress(route.getCoordinates().get(lastPoint).getLatitude(), route.getCoordinates().get(lastPoint).getLongitude(), getContext()));

            List<String> distanceVar = func.calculateDistance(route.getSummary().getLengthInMeters());
            TextView distance = vBottomSheet.findViewById(R.id.distance);
            distance.setText(String.format("%s%s", distanceVar.get(0), distanceVar.get(1)));

            TextView time = vBottomSheet.findViewById(R.id.travelTime);
            time.setText(func.formatTimeFromSeconds(route.getSummary().getTravelTimeInSeconds()));

            vBottomSheet.findViewById(R.id.buttonCancel).setOnClickListener(v -> cancel());
            vBottomSheet.findViewById(R.id.navigate).setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startNavigation(route);
            });
            bottomSheetDialog.show();
        } else {
            bottomSheetDialog.dismiss();
        }

    }

    private void startNavigation(FullRoute route){
        Icon activeIcon = Icon.Factory.fromResources(requireContext(), R.drawable.chevron_color, 2.5);
        Icon inactiveIcon = Icon.Factory.fromResources(requireContext(), R.drawable.chevron_shadow, 2.5);
        ChevronBuilder chevronBuilder = ChevronBuilder.create(activeIcon, inactiveIcon);
        chevron = tomtomMap.getDrivingSettings().addChevron(chevronBuilder);
        chevron.setLocation(uLocation);

        createMatcher(route);
        tomtomMap.set3DMode();
        tomtomMap.zoomTo(18);

        //setting constant location updates to update the UI with user position
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) { return; }
                for (Location location : locationResult.getLocations()) {
                    matcher.match(location);
                }
            }
        };
        startLocationUpdates();
        tomtomMap.getDrivingSettings().startTracking(chevron);

        //Updating Ui for navigation
        requireActivity().findViewById(R.id.searchViews).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.navigationBar).setVisibility(View.VISIBLE);
        ImageView _cancel = requireActivity().findViewById(R.id.cancelNavigation);
        _cancel.setOnClickListener(v -> stopNavigation());

        //Uploading trip data to firebase
        int size = route.getCoordinates().size()-1;
        String start = func.getAddress(route.getCoordinates().get(0).getLatitude(), route.getCoordinates().get(0).getLongitude(), getActivity());
        String end = func.getAddress(route.getCoordinates().get(size).getLatitude(), route.getCoordinates().get(size).getLongitude(), getActivity());
        Trips temp = new Trips(
                route.getCoordinates().get(0), start,
                route.getCoordinates().get(size), end,
                System.currentTimeMillis(),
                route.getSummary().getLengthInMeters());
        mDatabase.child("Trips").child(UUID.randomUUID().toString()).setValue(temp);
        //End Uploading to firebase
    }

    private void stopNavigation(){
        requireActivity().findViewById(R.id.searchViews).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.navigationBar).setVisibility(View.GONE);
        _DestinationLocationSearch.setText("");

        if (requestingLocationUpdates){
            fusedLocationClient.removeLocationUpdates(locationCallback);
            tomtomMap.getDrivingSettings().stopTracking();
            tomtomMap.clear();
            requestingLocationUpdates = false;
        }
    }

    private void cancel(){
        bottomSheetDialog.dismiss();
        tomtomMap.clear();
        _DestinationLocationSearch.setText("");
    }

    private void createMatcher(FullRoute route) {
        TextView _navigationText = requireActivity().findViewById(R.id.nextTurn);
        _navigationText.setText(route.getGuidance().getInstructions()[0].getMessage());

        TextView _timeLeft = requireActivity().findViewById(R.id.timeLeft);
        DateFormat simple = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        Date result = Objects.requireNonNull(route.getSummary().getArrivalTimeWithZone()).toDate();
        _timeLeft.setText(simple.format(result));

        TextView _turnDistance = requireActivity().findViewById(R.id.distanceToNextTurn);

        List<Integer> instructionIndex = new ArrayList<>();
        instructionIndex.add(0);

        matcher = MatcherFactory.createMatcher(LatLngTraceMatchingDataProvider.fromPoints(route.getCoordinates()));
        matcher.setMatcherListener(matchResult -> {
            chevron.setDimmed(!matchResult.isMatched());
            chevron.setLocation(matchResult.getMatchedLocation());

            List<Instruction> instructions = Arrays.asList(route.getGuidance().getInstructions());
            _turnDistance.setText(func.distanceToNextTurn(matchResult.getMatchedLocation().distanceTo(instructions.get(instructionIndex.get(0)).getPoint().toLocation())));
            double lat = Double.parseDouble(String.format(Locale.ENGLISH, "%.4f", matchResult.getMatchedLocation().getLatitude()));
            double lon = Double.parseDouble(String.format(Locale.ENGLISH, "%.4f", matchResult.getMatchedLocation().getLongitude()));
            LatLng temp = new LatLng(lat, lon);

            for ( int i = 0; i < instructions.size(); i++){
                lat = Double.parseDouble(String.format(Locale.ENGLISH, "%.4f", instructions.get(i).getPoint().getLatitude()));
                lon = Double.parseDouble(String.format(Locale.ENGLISH, "%.4f", instructions.get(i).getPoint().getLongitude()));
                LatLng temp2 = new LatLng(lat, lon);
                if (temp.getLatitude() == temp2.getLatitude() && temp.getLongitude() == temp2.getLongitude()){
                    _navigationText.setText(instructions.get(i+1).getMessage());
                    instructionIndex.clear();
                    instructionIndex.add(i+1);
                }
            }
            chevron.show();
        });
    }

    private void newMarker(LatLng location){
        tomtomMap.clear();
        tomtomMap.addMarker(new MarkerBuilder(location));
        tomtomMap.centerOn(location.getLatitude(), location.getLongitude(), 15, MapConstants.ORIENTATION_NORTH);
        setRoutePlanner(location);
    }

    private void newRoute(LatLng start, LatLng end, TravelMode transport){
        RoutingApi routingApi = OnlineRoutingApi.create(requireActivity());

        RouteQuery routeQuery = new RouteQueryBuilder(start, end)
                .withInstructionsType(InstructionsType.TEXT)
                .withConsiderTraffic(true)
                .withTravelMode(transport).build();

        routingApi.planRoute(routeQuery).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableSingleObserver<RouteResponse>() {
            @Override
            public void onSuccess(RouteResponse routeResponse) {
                for (FullRoute fullRoute : routeResponse.getRoutes()) {
                    tomtomMap.clear();

                    //builds the route on the map and displays for the user
                    RouteBuilder routeBuilder = new RouteBuilder(fullRoute.getCoordinates());
                    tomtomMap.addRoute(routeBuilder);
                    tomtomMap.displayRoutesOverview();

                    //dismisses the route planner bottom sheet and creates the navigation sheet
                    bottomSheetDialog.dismiss();
                    setNavigation(fullRoute);

                    //when user clicks on the route it brings the bottom sheet back up
                    tomtomMap.addOnRouteClickListener(route -> setNavigation(fullRoute));
                }
            }
            @Override
            public void onError(Throwable e) {
                Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initLocationSource() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        PermissionChecker permissionChecker = AndroidPermissionChecker.createLocationChecker(getActivity());
        if(permissionChecker.ifNotAllPermissionGranted()) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }

    private void getLastKnownLocation(){
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                uLocation = location;
                latLngCurrentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                tomtomMap.centerOn(uLocation.getLatitude(), uLocation.getLongitude(), 15);
            }
        });
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setFastestInterval(10).setInterval(100);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        requestingLocationUpdates = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length >= 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            } else {
                Toast.makeText(getActivity(), "Location Access Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopNavigation();
    }

    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {
        this.tomtomMap = tomtomMap;
        this.tomtomMap.setMyLocationEnabled(true);
        this.tomtomMap.clear();
    }
    private abstract static class BaseTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    }
}

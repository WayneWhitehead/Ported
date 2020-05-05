package com.hidesign.ported.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hidesign.ported.Functions
import com.hidesign.ported.R
import com.hidesign.ported.models.Trips
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.common.location.LatLngAcc
import com.tomtom.online.sdk.common.permission.AndroidPermissionChecker
import com.tomtom.online.sdk.common.permission.PermissionChecker
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.map.driving.*
import com.tomtom.online.sdk.map.model.MapTilesType
import com.tomtom.online.sdk.routing.OnlineRoutingApi
import com.tomtom.online.sdk.routing.data.*
import com.tomtom.online.sdk.search.OnlineSearchApi
import com.tomtom.online.sdk.search.SearchApi
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), OnMapReadyCallback, MatcherListener {
    private var requestingLocationUpdates = false
    private lateinit var uLocation: Location
    private lateinit var latLngCurrentPosition: LatLng
    private lateinit var latLngDestination: LatLng
    private lateinit var locationCallback: LocationCallback
    private lateinit var mDatabase: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tomtomMap: TomtomMap
    private lateinit var matcher: Matcher
    private lateinit var chevron: Chevron
    private lateinit var searchApi: SearchApi
    private val func = Functions()
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var locationSearch: AutoCompleteTextView
    private val searchTimerHandler = Handler()
    private var searchRunnable: Runnable? = null
    private lateinit var searchAdapter: ArrayAdapter<String>
    private val searchAutocompleteList: MutableList<String> = ArrayList()
    private val searchResultsMap: MutableMap<String, LatLng> = HashMap()
    private lateinit var navigationText: TextView
    private lateinit var timeLeft: TextView
    private lateinit var turnDistance: TextView
    private lateinit var navigationRoute: FullRoute
    private val instructionIndex: MutableList<Int> = ArrayList()

    private val onMapReadyCallback = OnMapReadyCallback { map ->
        tomtomMap = map
        tomtomMap.isMyLocationEnabled = true
        lastKnownLocation

        tomtomMap.uiSettings.mapTilesType = MapTilesType.VECTOR
        tomtomMap.uiSettings.compassView.hide()
        tomtomMap.trafficSettings.turnOnVectorTrafficFlowTiles()
        tomtomMap.trafficSettings.turnOnVectorTrafficIncidents()
        tomtomMap.markerSettings.setMarkersClustering(true)

        tomtomMap.addOnMapLongClickListener { latLng: LatLng -> newMarker(latLng) }
        tomtomMap.addOnMarkerClickListener { marker: Marker -> setRoutePlanner(marker.position) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity())
        mFirebaseAnalytics.setCurrentScreen(requireActivity(), "Home Fragment", "MapView")

        val mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference.child(mAuth.currentUser!!.uid)

        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        val openDrawer = root.findViewById<ImageView>(R.id.drawerButton)
        val navDrawer: DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        openDrawer.setOnClickListener { navDrawer.openDrawer(GravityCompat.START) }

        initLocationSource()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment
        mapFragment.getAsyncMap(onMapReadyCallback)

        searchApi = OnlineSearchApi.create(requireActivity())
        locationSearch = root.findViewById(R.id.atv_main_destination_location)
        searchAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_dropdown_item_1line, searchAutocompleteList)
        setTextWatcherToAutoCompleteField(locationSearch)

        return root
    }

    private fun setTextWatcherToAutoCompleteField(autoCompleteTextView: AutoCompleteTextView?) {
        autoCompleteTextView!!.setAdapter(searchAdapter)
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchTimerHandler.removeCallbacks(searchRunnable)
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    if (s.length >= 4) {
                        searchRunnable = Runnable { searchAddress(s.toString()) }
                        searchAdapter.clear()
                        searchTimerHandler.postDelayed(searchRunnable, 800)
                    }
                }
            }
        })
        autoCompleteTextView.onItemClickListener = OnItemClickListener { parent: AdapterView<*>, _: View?, position: Int, _: Long ->
            val item = parent.getItemAtPosition(position) as String
            if (autoCompleteTextView === locationSearch) {
                latLngDestination = searchResultsMap[item]!!
                newMarker(latLngDestination)
            }
        }
    }

    private fun searchAddress(searchWord: String) {
        searchApi.search(FuzzySearchQueryBuilder(searchWord).withTypeAhead(true).withMinFuzzyLevel(2)
                .withPreciseness(LatLngAcc(latLngCurrentPosition, 300000f)).build())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableSingleObserver<FuzzySearchResponse>() {
                    override fun onSuccess(fuzzySearchResponse: FuzzySearchResponse) {
                        if (!fuzzySearchResponse.results.isEmpty()) {
                            searchAutocompleteList.clear()
                            searchResultsMap.clear()
                            for (result in fuzzySearchResponse.results) {
                                val addressString = result.address.freeformAddress
                                searchAutocompleteList.add(addressString)
                                searchResultsMap[addressString] = result.position
                            }
                            searchAdapter.clear()
                            searchAdapter.addAll(searchAutocompleteList)
                            searchAdapter.filter.filter("")
                        }
                    }
                    override fun onError(e: Throwable) {
                        Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    @SuppressLint("InflateParams")
    private fun setRoutePlanner(marker: LatLng) {
        val transport: MaterialButtonToggleGroup
        val vBottomSheet: View = layoutInflater.inflate(R.layout.bottom_sheet_dialog_route_planner, null)

        bottomSheetDialog = BottomSheetDialog(requireActivity())
        bottomSheetDialog.setContentView(vBottomSheet)
        if (!bottomSheetDialog.isShowing) {
            uLocation = tomtomMap.userLocation!!
            val destination = Location("")
            destination.latitude = marker.latitude
            destination.longitude = marker.longitude
            val distanceVar = func.calculateDistance(uLocation.distanceTo(destination))
            val distance = vBottomSheet.findViewById<TextView>(R.id.distance)
            distance.text = String.format("%s%s", distanceVar[0], distanceVar[1])

            val address = vBottomSheet.findViewById<TextView>(R.id.nameAddress)
            address.text = func.getAddress(marker.latitude, marker.longitude, context)

            transport = vBottomSheet.findViewById(R.id.toggleButton)
            transport.isSingleSelection = true

            vBottomSheet.findViewById<View>(R.id.buttonCancel).setOnClickListener { cancel() }
            vBottomSheet.findViewById<View>(R.id.getDirections).setOnClickListener {
                newRoute(LatLng(uLocation.latitude, uLocation.longitude),
                        marker,
                        func.getTravelMode(transport.checkedButtonId))
            }
            bottomSheetDialog.show()
        } else {
            bottomSheetDialog.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    private fun setNavigation(route: FullRoute) {
        val vBottomSheet: View = layoutInflater.inflate(R.layout.bottom_sheet_dialog_start_navigation, null)

        bottomSheetDialog = BottomSheetDialog(requireActivity())
        bottomSheetDialog.setContentView(vBottomSheet)
        if (!bottomSheetDialog.isShowing) {
            uLocation = tomtomMap.userLocation!!
            val startAddress = vBottomSheet.findViewById<TextView>(R.id.startAddress)
            startAddress.text = func.getAddress(route.coordinates[0].latitude, route.coordinates[0].longitude, context)

            val endAddress = vBottomSheet.findViewById<TextView>(R.id.endAddress)
            val lastPoint = route.coordinates.size - 1
            endAddress.text = func.getAddress(route.coordinates[lastPoint].latitude, route.coordinates[lastPoint].longitude, context)

            val distanceVar = func.calculateDistance(route.summary.lengthInMeters.toFloat())
            val distance = vBottomSheet.findViewById<TextView>(R.id.distance)
            distance.text = String.format("%s%s", distanceVar[0], distanceVar[1])

            val time = vBottomSheet.findViewById<TextView>(R.id.travelTime)
            time.text = func.formatTimeFromSeconds(route.summary.travelTimeInSeconds.toLong())

            vBottomSheet.findViewById<View>(R.id.buttonCancel).setOnClickListener { cancel() }
            vBottomSheet.findViewById<View>(R.id.navigate).setOnClickListener {
                bottomSheetDialog.dismiss()
                startNavigation(route)
            }
            bottomSheetDialog.show()
        } else {
            bottomSheetDialog.dismiss()
        }
    }

    private fun startNavigation(route: FullRoute) {
        val activeIcon = Icon.Factory.fromResources(requireContext(), R.drawable.chevron_color, 2.5)
        val inactiveIcon = Icon.Factory.fromResources(requireContext(), R.drawable.chevron_shadow, 2.5)
        val chevronBuilder = ChevronBuilder.create(activeIcon, inactiveIcon)
        chevron = tomtomMap.drivingSettings.addChevron(chevronBuilder)
        chevron.setLocation(uLocation)
        createMatcher(route)
        tomtomMap.set3DMode()
        tomtomMap.zoomTo(18.0)
        tomtomMap.isMyLocationEnabled = false

        //setting constant location updates to update the UI with user position
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    matcher.match(location!!)
                }
            }
        }
        startLocationUpdates()
        tomtomMap.drivingSettings.startTracking(chevron)

        //Updating Ui for navigation
        requireActivity().findViewById<View>(R.id.searchViews).visibility = View.GONE
        requireActivity().findViewById<View>(R.id.navigationBar).visibility = View.VISIBLE
        val cancel = requireActivity().findViewById<ImageView>(R.id.cancelNavigation)
        cancel.setOnClickListener { stopNavigation() }

        //Uploading trip data to firebase
        val size = route.coordinates.size - 1
        val start = func.getAddress(route.coordinates[0].latitude, route.coordinates[0].longitude, activity)
        val end = func.getAddress(route.coordinates[size].latitude, route.coordinates[size].longitude, activity)
        val temp = Trips(
                route.coordinates[0], start,
                route.coordinates[size], end,
                System.currentTimeMillis(),
                route.summary.lengthInMeters.toFloat())
        mDatabase.child("Trips").child(UUID.randomUUID().toString()).setValue(temp)
        //End Uploading to firebase
    }

    private fun stopNavigation() {
        requireActivity().findViewById<View>(R.id.searchViews).visibility = View.VISIBLE
        requireActivity().findViewById<View>(R.id.navigationBar).visibility = View.GONE
        locationSearch.setText("")
        if (requestingLocationUpdates) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            matcher.dispose()
            tomtomMap.drivingSettings.stopTracking()
            tomtomMap.clear()
            requestingLocationUpdates = false
            tomtomMap.isMyLocationEnabled = true
        }
    }

    private fun cancel() {
        bottomSheetDialog.dismiss()
        tomtomMap.clear()
        locationSearch.setText("")
    }

    private fun createMatcher(route: FullRoute) {
        navigationText = requireActivity().findViewById(R.id.nextTurn)
        navigationText.text = route.guidance.instructions[0].message

        timeLeft = requireActivity().findViewById(R.id.timeLeft)
        val simple: DateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val result = route.summary.arrivalTimeWithZone!!.toDate()
        timeLeft.text = simple.format(result)

        turnDistance = requireActivity().findViewById(R.id.distanceToNextTurn)
        instructionIndex.add(0)

        navigationRoute = route
        matcher = MatcherFactory.createMatcher(LatLngTraceMatchingDataProvider.fromPoints(route.coordinates))
        matcher.setMatcherListener(this)
    }

    override fun onMatched(matchResult: MatchResult) {
        chevron.isDimmed = !matchResult.isMatched
        chevron.setLocation(matchResult.matchedLocation)
        val instructions = navigationRoute.guidance.instructions.toList()
        turnDistance.text = func.distanceToNextTurn(matchResult.matchedLocation.distanceTo(instructions[instructionIndex[0]].point.toLocation()))
        var lat = String.format(Locale.ENGLISH, "%.4f", matchResult.matchedLocation.latitude).toDouble()
        var lon = String.format(Locale.ENGLISH, "%.4f", matchResult.matchedLocation.longitude).toDouble()
        val temp = LatLng(lat, lon)
        for (i in instructions.indices) {
            lat = String.format(Locale.ENGLISH, "%.4f", instructions[i].point.latitude).toDouble()
            lon = String.format(Locale.ENGLISH, "%.4f", instructions[i].point.longitude).toDouble()
            val temp2 = LatLng(lat, lon)
            if (temp.latitude == temp2.latitude && temp.longitude == temp2.longitude) {
                navigationText.text = instructions[i + 1].message
                instructionIndex.clear()
                instructionIndex.add(i + 1)
            }
        }
        chevron.show()
    }

    private fun newMarker(location: LatLng) {
        tomtomMap.clear()
        tomtomMap.addMarker(MarkerBuilder(location))
        @Suppress("DEPRECATION")
        tomtomMap.centerOn(location.latitude, location.longitude, 15.0)
        setRoutePlanner(location)
    }

    private fun newRoute(start: LatLng, end: LatLng, transport: TravelMode) {
        val routingApi = OnlineRoutingApi.create(requireActivity())
        val routeQuery = RouteQueryBuilder(start, end)
                .withInstructionsType(InstructionsType.TEXT)
                .withConsiderTraffic(true)
                .withTravelMode(transport).build()

        routingApi.planRoute(routeQuery).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : DisposableSingleObserver<RouteResponse?>() {
            override fun onSuccess(routeResponse: RouteResponse) {
                for (fullRoute in routeResponse.routes) {
                    tomtomMap.clear()

                    //builds the route on the map and displays for the user
                    val routeBuilder = RouteBuilder(fullRoute.coordinates)
                    tomtomMap.addRoute(routeBuilder)
                    tomtomMap.displayRouteOverview(routeBuilder.id)

                    //dismisses the route planner bottom sheet and creates the navigation sheet
                    bottomSheetDialog.dismiss()
                    setNavigation(fullRoute)

                    //when user clicks on the route it brings the bottom sheet back up
                    tomtomMap.addOnRouteClickListener { setNavigation(fullRoute) }
                }
            }
            override fun onError(e: Throwable) {
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun initLocationSource() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val permissionChecker: PermissionChecker = AndroidPermissionChecker.createLocationChecker(activity)
        if (permissionChecker.ifNotAllPermissionGranted()) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private val lastKnownLocation: Unit
        get() {
            fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location: Location? ->
                if (location != null) {
                    uLocation = location
                    latLngCurrentPosition = LatLng(location.latitude, location.longitude)
                    @Suppress("DEPRECATION")
                    tomtomMap.centerOn(uLocation.latitude, uLocation.longitude, 15.0)
                }
            }
        }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setFastestInterval(10).setInterval(100)
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        requestingLocationUpdates = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size >= 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                lastKnownLocation
            } else {
                Toast.makeText(activity, "Location Access Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopNavigation()
    }

    override fun onMapReady(tomtomMap: TomtomMap) {
        this.tomtomMap = tomtomMap
        this.tomtomMap.isMyLocationEnabled = true
        this.tomtomMap.clear()
    }
}

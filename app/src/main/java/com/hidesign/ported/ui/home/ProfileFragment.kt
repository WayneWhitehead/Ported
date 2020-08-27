package com.hidesign.ported.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hidesign.ported.Functions
import com.hidesign.ported.R
import com.hidesign.ported.models.Trips
import com.tomtom.online.sdk.routing.data.TravelMode
import timber.log.Timber
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var fab: FloatingActionButton
    private val pastTrips: MutableList<Trips> = ArrayList()
    private lateinit var mDatabase: DatabaseReference
    private val func = Functions()
    private lateinit var transport: MaterialButtonToggleGroup
    private lateinit var measurement: MaterialButtonToggleGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewInflate = inflater.inflate(R.layout.fragment_profile, container, false)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference.child(mAuth.currentUser!!.uid)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        if (mAuth.currentUser?.displayName == null){
            viewInflate.findViewById<CollapsingToolbarLayout>(R.id.ToolbarLayout).title = mAuth.currentUser?.email
        } else {
            viewInflate.findViewById<CollapsingToolbarLayout>(R.id.ToolbarLayout).title = mAuth.currentUser?.displayName
        }

        fab = viewInflate.findViewById(R.id.actionMenu)
        val navDrawer: DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        fab.setOnClickListener { navDrawer.openDrawer(GravityCompat.START) }

        transport = viewInflate.findViewById(R.id.transportToggle)
        transport.isSingleSelection = true
        transport.setOnClickListener {
            val travel = func.getTravelMode(transport.checkedButtonId)
            if (travel == TravelMode.CAR) { mDatabase.child("Settings").child("Transport").setValue(0) }
            if (travel == TravelMode.PEDESTRIAN) { mDatabase.child("Settings").child("Transport").setValue(1) }
        }

        measurement = viewInflate.findViewById(R.id.measurementToggle)
        measurement.isSingleSelection = true
        measurement.setOnClickListener {
            val measure = func.getMesurementMode(measurement.checkedButtonId)
            Timber.e(measurement.checkedButtonId.toString(), "" )
            mDatabase.child("Settings").child("Measurement").setValue(measure)
        }

        loadTripInformation()

        return viewInflate
    }
    private fun loadTripInformation (){
        mDatabase.child("Trips").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(Trips::class.java)
                    pastTrips.add(post!!)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}

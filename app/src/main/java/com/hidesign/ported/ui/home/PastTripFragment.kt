package com.hidesign.ported.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hidesign.ported.Functions
import com.hidesign.ported.R
import com.hidesign.ported.adapters.RecyclerAdapter
import com.hidesign.ported.models.Trips
import timber.log.Timber
import java.util.*

class PastTripFragment : Fragment() {

    private lateinit var mRecyclerView: RecyclerView
    private val pastTrips: MutableList<Trips> = ArrayList()
    private val func = Functions()
    private var mDatabase: DatabaseReference? = null
    private var mProgressBar: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewInflate = inflater.inflate(R.layout.fragment_past_trips, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        mProgressBar = viewInflate.findViewById(R.id.progress_bar)

        mRecyclerView = viewInflate.findViewById(R.id.recyclerView)
        mRecyclerView.setHasFixedSize(true)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        mRecyclerView.layoutManager = mLayoutManager

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            mDatabase = FirebaseDatabase.getInstance().reference.child(currentUser.uid)
        }

        return viewInflate
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDatabase!!.child("Trips").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(Trips::class.java)
                    pastTrips.add(post!!)
                }
                func.sortList(pastTrips)
                showList(pastTrips)
                mProgressBar!!.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun showList(display: List<Trips?>) {
        Timber.e("showList: %s", pastTrips.size)
        val mAdapter = RecyclerAdapter(display.size, R.layout.item_trip)
        mAdapter.setOnRecyclerAdapterListener { _: RecyclerAdapter?, v: RecyclerAdapter.ViewHolder, position: Int ->
            val date = v.view.findViewById<TextView>(R.id.Date)
            date.text = func.getDate(display[position]!!.tripDate, "dd MMM yyyy HH:mm")

            val startAddress = v.view.findViewById<TextView>(R.id.StartLocation)
            startAddress.text = display[position]!!.startAddress
            val endAddress = v.view.findViewById<TextView>(R.id.EndLocation)
            endAddress.text = display[position]!!.endAddress

            val distance = func.calculateDistance(display[position]!!.tripDistance)
            val distanceDisplay = v.view.findViewById<TextView>(R.id.Distance)
            distanceDisplay.text = distance[0]
            val distanceSystem = v.view.findViewById<TextView>(R.id.DistanceSystem)
            distanceSystem.text = distance[1]
        }
        mRecyclerView.adapter = mAdapter
    }
}
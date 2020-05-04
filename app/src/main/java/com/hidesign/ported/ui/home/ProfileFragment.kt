package com.hidesign.ported.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.hidesign.ported.R

class ProfileFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewInflate = inflater.inflate(R.layout.fragment_profile, container, false)
        mAuth = FirebaseAuth.getInstance()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        if (mAuth.currentUser?.displayName == null){
            viewInflate.findViewById<CollapsingToolbarLayout>(R.id.ToolbarLayout).title = mAuth.currentUser?.email
        } else {
            viewInflate.findViewById<CollapsingToolbarLayout>(R.id.ToolbarLayout).title = mAuth.currentUser?.displayName
        }

        fab = viewInflate.findViewById(R.id.actionMenu)
        val navDrawer: DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        fab.setOnClickListener { navDrawer.openDrawer(GravityCompat.START) }

        return viewInflate
    }
}

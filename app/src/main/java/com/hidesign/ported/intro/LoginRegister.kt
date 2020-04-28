package com.hidesign.ported.intro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.hidesign.ported.R
import java.util.*

class LoginRegister : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signOut()
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val pagerAdapter = AuthenticationPagerAdapter(supportFragmentManager)
        pagerAdapter.addFragment(Login())
        pagerAdapter.addFragment(Register())
        viewPager.adapter = pagerAdapter
    }

    internal class AuthenticationPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        private val fragmentList = ArrayList<Fragment>()
        override fun getItem(i: Int): Fragment {
            return fragmentList[i]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }
    }
}
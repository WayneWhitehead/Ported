package com.hidesign.ported.intro

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.hidesign.ported.R
import kotlin.math.abs

class LoginRegister : AppCompatActivity() {

    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        viewPager2 = findViewById(R.id.viewPager)

        val fragmentList = arrayListOf(Login(), Register())
        viewPager2.adapter = ViewPagerAdapter(this, fragmentList)
        viewPager2.setPageTransformer(DepthPageTransformer())
    }

    inner class ViewPagerAdapter(fa: FragmentActivity, private val fragments:ArrayList<Fragment>): FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    override fun onBackPressed() {
        if (viewPager2.currentItem == 0){
            viewPager2.currentItem = 1
        } else {
            viewPager2.currentItem = 0
        }
    }

    inner class DepthPageTransformer : ViewPager2.PageTransformer {

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 0 -> { // [-1,0]
                        // Use the default slide transition when moving to the left page
                        alpha = 1f
                        translationX = 0f
                        translationZ = 0f
                        scaleX = 1f
                        scaleY = 1f
                    }
                    position <= 1 -> { // (0,1]
                        // Fade the page out.
                        alpha = 1 - position

                        // Counteract the default slide transition
                        translationX = -1f
                        // Move it behind the left page
                        translationZ = pageHeight * -position

                        // Scale the page down (between MIN_SCALE and 1)
                        val scaleFactor = (0.75 + (1 - 0.75) * (1 - abs(position)))
                        scaleX = scaleFactor.toFloat()
                        scaleY = scaleFactor.toFloat()
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }
}
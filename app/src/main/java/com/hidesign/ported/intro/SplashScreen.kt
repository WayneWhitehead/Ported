package com.hidesign.ported.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hidesign.ported.MainActivity
import com.hidesign.ported.R
import java.util.*

class SplashScreen : AppCompatActivity() {
    var currentUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser
        showMain()
    }

    private fun showMain() {
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                if (currentUser != null) {
                    goToMain()
                } else {
                    goToSignIn()
                }
            }
        }
        Timer().schedule(task, 2000)
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToSignIn() {
        val intent = Intent(this, LoginRegister::class.java)
        startActivity(intent)
        finish()
    }
}
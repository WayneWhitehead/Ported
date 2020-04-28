package com.hidesign.ported.intro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.hidesign.ported.MainActivity
import com.hidesign.ported.R
import timber.log.Timber
import java.util.*

class Register : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var _username: TextInputEditText
    private lateinit var _password: TextInputEditText
    private lateinit var _repeatPassword: TextInputEditText
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_register, container, false)

        mAuth = FirebaseAuth.getInstance()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity())
        mFirebaseAnalytics.setCurrentScreen(requireActivity(), "Register Fragment", "Register")

        _username = layout.findViewById(R.id.username)
        _password = layout.findViewById(R.id.password)
        _repeatPassword = layout.findViewById(R.id.repeatPassword)

        val submit: MaterialButton = layout.findViewById(R.id.btn_register)
        submit.setOnClickListener { checkLogin() }

        return layout
    }

    private fun checkLogin() {
        if (validate()) {
            mAuth.createUserWithEmailAndPassword(_username.text.toString(), _password.text.toString())
                    .addOnCompleteListener(requireActivity()) { task: Task<AuthResult?> ->
                        if (task.isSuccessful) {
                            Timber.d("createUserWithEmail:success")
                            val params = Bundle()
                            params.putString("User", Objects.requireNonNull(mAuth.currentUser?.email))
                            mFirebaseAnalytics.logEvent("registerWithEmail", params)
                            goToMain()
                        } else {
                            Timber.w(task.exception, "createUserWithEmail:failure")
                            Toast.makeText(activity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun goToMain() {
        val intent = Intent(context, MainActivity::class.java)
        startActivityForResult(intent, Activity.RESULT_OK)
        requireActivity().finish()
    }

    private fun validate(): Boolean {
        return when {
            _username.text.toString() == "" -> {
                _username.error = "Username cannot be empty"
                false
            }
            _password.text.toString().length < 6 -> {
                _password.error = "Password cannot be less than 8 characters"
                false
            }
            _password.text.toString() != _repeatPassword.text.toString() -> {
                _repeatPassword.error = "Passwords do not match"
                false
            }
            else -> true
        }
    }
}
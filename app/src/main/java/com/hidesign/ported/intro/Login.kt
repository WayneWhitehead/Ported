package com.hidesign.ported.intro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hidesign.ported.MainActivity
import com.hidesign.ported.R
import timber.log.Timber
import java.util.*

class Login : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var _username: TextInputEditText
    private lateinit var _password: TextInputEditText
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_login, container, false)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity())
        mFirebaseAnalytics!!.setCurrentScreen(requireActivity(), "Login Fragment", "Login")
        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        _username = layout.findViewById(R.id.username)
        _username.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        _password = layout.findViewById(R.id.password)
        _password.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)

        val google: SignInButton = layout.findViewById(R.id.google)
        google.setOnClickListener { signIn() }
        val login: MaterialButton = layout.findViewById(R.id.btn_login)
        login.setOnClickListener { checkLogin() }

        return layout
    }

    private fun checkLogin() {
        if (validate()) {
            mAuth.signInWithEmailAndPassword(Objects.requireNonNull(_username.text).toString(), Objects.requireNonNull(_password.text).toString())
                    .addOnCompleteListener(requireActivity()) { task: Task<AuthResult?> ->
                        if (task.isSuccessful) {
                            Timber.d("signInWithEmail:success")
                            val params = Bundle()
                            params.putString("User", Objects.requireNonNull(mAuth.currentUser?.email))
                            mFirebaseAnalytics!!.logEvent("loginWithEmail", params)
                            goToMain()
                        } else {
                            Timber.tag("Login").w(task.exception, "signInWithEmail:failure")
                        }
                    }
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Timber.tag("LoginFragment").e(e, "Google sign in failed")
                Toast.makeText(activity, "Google sign in failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Timber.tag("GoogleSignin").e("firebaseAuthWithGoogle:%s", acct.id)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task: Task<AuthResult?> ->
            if (task.isSuccessful) {
                Timber.tag("GoogleSignIn").e("signInWithCredential:success")
                val params = Bundle()
                params.putString("User", Objects.requireNonNull(mAuth.currentUser?.email))
                mFirebaseAnalytics!!.logEvent("loginWithGoogle", params)
                goToMain()
            } else {
                Timber.tag("GoogleSignIn").e(task.exception, "signInWithCredential:failure")
                Toast.makeText(activity, "signInWithCredential:failure.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validate(): Boolean {
        if (Objects.requireNonNull(_username.text).toString() == "") {
            _username.error = "Username cannot be empty"
            return false
        } else if (Objects.requireNonNull(_password.text).toString().length < 6) {
            _password.error = "Password cannot be less than 8 characters"
            return false
        }
        return true
    }

    private fun goToMain() {
        val intent = Intent(context, MainActivity::class.java)
        startActivityForResult(intent, Activity.RESULT_OK)
        requireActivity().finish()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
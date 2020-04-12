package com.hidesign.ported.intro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.hidesign.ported.MainActivity;
import com.hidesign.ported.R;

import java.util.Objects;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class Login extends Fragment {

    private FirebaseAuth mAuth;
    private TextInputEditText _username, _password;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_login, container, false);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "Login Fragment", "Login");

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gso);

        _username = layout.findViewById(R.id.username);
        _password = layout.findViewById(R.id.password);

        SignInButton _google = layout.findViewById(R.id.google);
        _google.setOnClickListener(v -> signIn());

        MaterialButton _login = layout.findViewById(R.id.btn_login);
        _login.setOnClickListener(v -> CheckLogin());

        return layout;
    }

    private void CheckLogin(){
        if (Validate()){
            mAuth.signInWithEmailAndPassword(Objects.requireNonNull(_username.getText()).toString(), Objects.requireNonNull(_password.getText()).toString())
                    .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                        if (task.isSuccessful()) {
                            Timber.d("signInWithEmail:success");
                            Bundle params = new Bundle();
                            params.putString("User", mAuth.getCurrentUser().getEmail());
                            mFirebaseAnalytics.logEvent("loginWithEmail", params);
                            GoToMain();
                        } else {
                            Timber.tag("Login").w(task.getException(), "signInWithEmail:failure");
                        }
                    });
        }
    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Timber.tag("LoginFragment").e(e, "Google sign in failed");
                Toast.makeText(getActivity(), "Google sign in failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Timber.tag("GoogleSignin").e("firebaseAuthWithGoogle:%s", acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
            if (task.isSuccessful()) {
                Timber.tag("GoogleSignIn").e("signInWithCredential:success");
                Bundle params = new Bundle();
                params.putString("User", mAuth.getCurrentUser().getEmail());
                mFirebaseAnalytics.logEvent("loginWithGoogle", params);
                GoToMain();
            } else {
                Timber.tag("GoogleSignIn").e(task.getException(), "signInWithCredential:failure");
                Toast.makeText(getActivity(), "signInWithCredential:failure.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Boolean Validate() {
        if (Objects.requireNonNull(_username.getText()).toString().equals("")){
            _username.setError("Username cannot be empty");
            return false;
        }
        else if (Objects.requireNonNull(_password.getText()).toString().length() < 6) {
            _password.setError("Password cannot be less than 8 characters");
            return false;
        }
        return true;
    }

    private void GoToMain(){
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivityForResult(intent, RESULT_OK);
        Objects.requireNonNull(getActivity()).finish();
    }
}

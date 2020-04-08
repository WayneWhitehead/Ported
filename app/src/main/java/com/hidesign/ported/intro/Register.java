package com.hidesign.ported.intro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hidesign.ported.MainActivity;
import com.hidesign.ported.R;

import java.util.Objects;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class Register extends Fragment {

    private final String TAG = "RegisterFrament";
    private FirebaseAuth mAuth;
    private TextInputEditText _username, _password, _repeatPassword;
    private static final int RC_SIGN_IN = 9001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_register, container, false);
        mAuth = FirebaseAuth.getInstance();

        _username = layout.findViewById(R.id.username);
        _password = layout.findViewById(R.id.password);
        _repeatPassword = layout.findViewById(R.id.repeatPassword);

        MaterialButton _submit = layout.findViewById(R.id.btn_register);
        _submit.setOnClickListener(v -> CheckLogin());

        return layout;
    }

    private void CheckLogin(){
        if (Validate()){
            mAuth.createUserWithEmailAndPassword(_username.getText().toString(), _password.getText().toString()).addOnCompleteListener(getActivity(), task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "createUserWithEmail:success");
                    GoToMain();
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void GoToMain(){
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivityForResult(intent, RESULT_OK);
        Objects.requireNonNull(getActivity()).finish();
    }

    private Boolean Validate() {
        if (_username.getText().toString().equals("")){
            _username.setError("Username cannot be empty");
            return false;
        }
        else if (_password.getText().toString().length() < 6) {
            _password.setError("Password cannot be less than 8 characters");
            return false;
        } else if (!_password.getText().toString().equals(_repeatPassword.getText().toString())){
            _repeatPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }
}

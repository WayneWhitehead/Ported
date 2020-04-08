package com.hidesign.ported.ui.home;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import com.hidesign.ported.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private String TAG = "Settings";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
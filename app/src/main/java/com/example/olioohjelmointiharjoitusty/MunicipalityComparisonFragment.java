//Read this you handsome ;)
//THis fragment is mad for comparing the betveen city data, Amazing!

package com.example.olioohjelmointiharjoitusty.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.olioohjelmointiharjoitusty.R;

public class MunicipalityComparisonFragment extends Fragment {

    public MunicipalityComparisonFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_municipality_comparison, container, false);
    }
}
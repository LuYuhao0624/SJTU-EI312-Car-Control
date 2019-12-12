package com.car;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AbsoluteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AbsoluteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AbsoluteFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_absolute, container, false);
        return root;
    }


}

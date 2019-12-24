package com.car;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class BasicFragment extends Fragment {
	private Bluetooth bluetooth;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_basic, container, false);

		MainActivity mainActivity = (MainActivity)getActivity();
		bluetooth = mainActivity.bluetooth;

		Button left = root.findViewById(R.id.btn_left);
		Button right = root.findViewById(R.id.btn_right);
		Button forward = root.findViewById(R.id.btn_forward);
		Button stop = root.findViewById(R.id.btn_stop);

		View.OnClickListener clickHandler = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(v.getId()){
					case R.id.btn_left:
						bluetooth.send(2);
						break;
					case R.id.btn_right:
						bluetooth.send(3);
						break;
					case R.id.btn_forward:
						bluetooth.send(1);
						break;
					case R.id.btn_stop:
						bluetooth.send(0);
						break;
				}
			}
		};

		left.setOnClickListener(clickHandler);
		right.setOnClickListener(clickHandler);
		forward.setOnClickListener(clickHandler);
		stop.setOnClickListener(clickHandler);
		return root;
	}
}

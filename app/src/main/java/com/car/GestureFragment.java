package com.car;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class GestureFragment extends Fragment {

	private Bluetooth bluetooth;
	private GestureDetector gesture_detector;

	// least displacement to recognize as a gesture control
	private static final float LEAST_DISPLACEMENT = 200;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_gesture, container, false);

		MainActivity main_activity = (MainActivity)getActivity();
		bluetooth = main_activity.bluetooth;

		// gesture detector for gesture control
		gesture_detector = new GestureDetector(this.getActivity(), new GestureDetector.OnGestureListener() {
			// this method must return true, otherwise onFling does not work
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public void onShowPress(MotionEvent e) {	}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {	}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				float x_displacement = e2.getX() - e1.getX();
				float y_displacement = e2.getY() - e1.getY();
				float abs_x_displacement = Math.abs(x_displacement);
				float abs_y_displacement = Math.abs(y_displacement);

				if (Math.max(Math.abs(x_displacement), Math.abs(y_displacement)) < LEAST_DISPLACEMENT) {
					Toast.makeText(getActivity().getBaseContext(), "Move too short. Try a longer move.", Toast.LENGTH_SHORT).show();
					return false;
				}

				if (abs_x_displacement > abs_y_displacement) {
					if (x_displacement > 0) {
						Toast.makeText(getActivity().getBaseContext(), "Turn right.", Toast.LENGTH_SHORT).show();
						// turn right
						bluetooth.send(3);
					}
					else {
						Toast.makeText(getActivity().getBaseContext(), "Turn left.", Toast.LENGTH_SHORT).show();
						// turn left
						bluetooth.send(2);
					}
					return true;
				}
				else if (abs_x_displacement < abs_y_displacement) {
					if (y_displacement > 0) {
						Toast.makeText(getActivity().getBaseContext(), "Stop.", Toast.LENGTH_SHORT).show();
						// stop
						bluetooth.send(0);
					}
					else {
						Toast.makeText(getActivity().getBaseContext(), "Move forward.", Toast.LENGTH_SHORT).show();
						// forward
						bluetooth.send(1);
					}
					return true;
				}
				else {
					Toast.makeText(getActivity().getBaseContext(), "Move distance too close on two axises.", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		});

		root.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gesture_detector.onTouchEvent(event);
			}
		});

		return root;
	}
}
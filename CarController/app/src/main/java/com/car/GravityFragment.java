package com.car;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import static android.content.Context.SENSOR_SERVICE;

public class GravityFragment extends Fragment {

    private Bluetooth bluetooth;
    private MainActivity main_activity;
    private Context base_context;
    private DirectionSensor direction_sensor;

	private int old_control = 0;
	private int new_control = 0;

    // least pitch to turn left/right
    private static final int LEAST_PITCH = 20;
    // least roll to recognize as forward
    private static final int LEAST_ROLL = -45;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gravity, container, false);
        main_activity = (MainActivity)getActivity();
        base_context = main_activity.getBaseContext();
        this.bluetooth = main_activity.bluetooth;

        direction_sensor = new DirectionSensor(base_context, null) {
			@Override
			public void processDataOrSendSignal(int azimuth, int pitch, int roll) {
				decodePitchRollOrSendControl(pitch, roll);
			}
		};
        return root;
    }

    public void onResume() {
        super.onResume();
        direction_sensor.registerSensor();
    }

    //onPause() unregister the accelerometer for stop listening the events
    public void onPause() {
        direction_sensor.unregisterSensor();
		super.onPause();
    }

    private void decodePitchRollOrSendControl(int pitch, int roll) {
		if (pitch > LEAST_PITCH) {
			new_control = 2;
		}
		else if (pitch < -LEAST_PITCH) {
			new_control = 3;
		}
		else if (roll > LEAST_ROLL) {
			new_control = 1;
		}
		else {
			new_control = 0;
		}
		if (old_control != new_control) {
			if (new_control == 0) {
				Toast.makeText(base_context, "Stop.", Toast.LENGTH_SHORT).show();
			}
			else if(new_control == 1) {
				Toast.makeText(base_context, "Forward.", Toast.LENGTH_SHORT).show();
			}
			else if(new_control == 2) {
				Toast.makeText(base_context, "Left.", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(base_context, "Right.", Toast.LENGTH_SHORT).show();
			}
			bluetooth.send(new_control);
		}
		old_control = new_control;
	}
}

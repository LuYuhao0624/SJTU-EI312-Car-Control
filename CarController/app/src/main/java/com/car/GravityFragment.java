package com.car;

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

public class GravityFragment extends Fragment implements SensorEventListener {

    private Bluetooth bluetooth;
    private SensorManager sensor_manager;
    private Sensor accelerometer, magnetometer;

    // least pitch to turn left/right
    private static final float LEAST_PITCH = 0.4f;
    // least roll to recognize as forward
    private static final float LEAST_ROLL = -0.6f;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gravity, container, false);
        MainActivity main_activity = (MainActivity)getActivity();
        bluetooth = main_activity.bluetooth;
        sensor_manager = (SensorManager)getActivity().getSystemService(SENSOR_SERVICE);
        accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        return root;
    }

    public void onResume() {
        super.onResume();
        sensor_manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensor_manager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    //onPause() unregister the accelerometer for stop listening the events
    public void onPause() {
        super.onPause();
        sensor_manager.unregisterListener(this);
    }

    float[] gravity, geomagnetic;
    int old_control = 0; // 0: stop, 1: forward, 2: left, 3: right
    int new_control = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			gravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			geomagnetic = event.values;
		if (gravity != null && geomagnetic != null) {
			float[] R = new float[9];
			float[] I = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				float pitch = orientation[1];
				float roll = orientation[2];
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
						Toast.makeText(getActivity().getBaseContext(), "Stop.", Toast.LENGTH_SHORT).show();
					}
					else if(new_control == 1) {
						Toast.makeText(getActivity().getBaseContext(), "Forward.", Toast.LENGTH_SHORT).show();
					}
					else if(new_control == 2) {
						Toast.makeText(getActivity().getBaseContext(), "Left.", Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(getActivity().getBaseContext(), "Right.", Toast.LENGTH_SHORT).show();
					}
					bluetooth.send(new_control);
				}
				old_control = new_control;
			}
		}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {	}
}

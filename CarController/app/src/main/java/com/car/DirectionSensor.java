package com.car;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.SENSOR_SERVICE;

public abstract class DirectionSensor implements SensorEventListener {
	private SensorManager sensor_manager;
	private Sensor accelerometer, magnetometer;
	private Context main_context;

	private float[] gravity, geomagnetic;
	private int azimuth = 0;
	private int pitch = 0;
	private int roll = 0;
	private TextView view;

	DirectionSensor(Context context, TextView view) {
		main_context = context;
		sensor_manager = (SensorManager)main_context.getSystemService(SENSOR_SERVICE);
		accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		this.view = view;
	}

	void registerSensor() {
		sensor_manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensor_manager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}

	void unregisterSensor() {
		sensor_manager.unregisterListener(this);
	}

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
				float[] orientation = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimuth = (int)Math.toDegrees(orientation[0]);
				pitch = (int)Math.toDegrees(orientation[1]);
				roll = (int)Math.toDegrees(orientation[2]);
				if (view != null)
					view.setText("Azimuth:" + azimuth);
				processDataOrSendSignal(azimuth, pitch, roll);
			}
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) { }

	public abstract void processDataOrSendSignal(int azimuth, int pitch, int roll);
}

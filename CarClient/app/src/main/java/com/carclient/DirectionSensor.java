
package com.carclient;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import static android.content.Context.SENSOR_SERVICE;

public class DirectionSensor implements SensorEventListener {
	private SensorManager sensor_manager;
	private Sensor accelerometer, magnetometer;
	private Context main_context;
	private WifiClient wifi;

	private float[] gravity, geomagnetic;
	private int azimuth = 0;
	private TextView azimuth_view;

	DirectionSensor(Context context, WifiClient wifi, TextView azimuth_view) {
		main_context = context;
		sensor_manager = (SensorManager)main_context.getSystemService(SENSOR_SERVICE);
		accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		this.wifi = wifi;
		this.azimuth_view = azimuth_view;
	}

	void registerSensor() {
		sensor_manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
		sensor_manager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
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
				azimuth_view.setText(String.valueOf(azimuth));
			}
		}
		new Thread(){
			@Override public void run(){
				wifi.send(azimuth);
			}
		}.start();
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {	}
}
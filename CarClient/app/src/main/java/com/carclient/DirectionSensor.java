package com.carclient;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.SENSOR_SERVICE;

public class DirectionSensor implements SensorEventListener {
	private SensorManager sensor_manager;
	private Sensor accelerometer, magnetometer;
	private Context main_context;
	private WifiClient wifi;

	private static final int BUFFER_WIDTH = 10;
	private static final int EAST_LEFT = -10; // the y-axis of phone is not the car head direction
	private static final int EAST_RIGHT = 10;
	private static final int EL_BUFFER = EAST_LEFT - BUFFER_WIDTH;
	private static final int ER_BUFFER = EAST_RIGHT + BUFFER_WIDTH;
	private static final int WEST_LEFT = 170;
	private static final int WEST_MID = 180;
	private static final int WEST_RIGHT = -170;
	private static final int WR_BUFFER = WEST_RIGHT + BUFFER_WIDTH;
	private static final int WL_BUFFER = WEST_LEFT - BUFFER_WIDTH;
	private static final int NORTH_LEFT = -100;
	private static final int NORTH_RIGHT = -80;
	private static final int NL_BUFFER = NORTH_LEFT - BUFFER_WIDTH;
	private static final int NR_BUFFER = NORTH_RIGHT + BUFFER_WIDTH;
	private static final int SOUTH_LEFT = 80;
	private static final int SOUTH_RIGHT = 100;
	private static final int SL_BUFFER = SOUTH_LEFT - BUFFER_WIDTH;
	private static final int SR_BUFFER = SOUTH_RIGHT + BUFFER_WIDTH;

	private static final int NORTH = 0;
	private static final int WEST = 2;
	private static final int SOUTH = 4;
	private static final int EAST = 6;
	private static final int NORTHWEST = 1;
	private static final int SOUTHWEST = 3;
	private static final int SOUTHEAST = 5;
	private static final int NORTHEAST = 7;

	private float[] gravity, geomagnetic;
	private int old_direction = 0; // 0: north, 1: east, 2: south, 3: west
	private int new_direction = 0; // 10: northeast, 30: northwest, 12 southeast, 32: southwest
	public int azimuth = 0;
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
				azimuth_view.setText("Azimuth:" + azimuth);

				if (inInterval(NORTH_LEFT, NORTH_RIGHT, azimuth))
					new_direction = NORTH;
				else if (inInterval(-WEST_MID, WEST_RIGHT, azimuth) || inInterval(WEST_LEFT, WEST_MID, azimuth))
					new_direction = WEST;
				else if (inInterval(EAST_LEFT, EAST_RIGHT, azimuth))
					new_direction = EAST;
				else if (inInterval(SOUTH_LEFT, SOUTH_RIGHT, azimuth))
					new_direction = SOUTH;
				else if (inInterval(NR_BUFFER, EL_BUFFER, azimuth))
					new_direction = NORTHEAST;
				else if (inInterval(ER_BUFFER, SL_BUFFER, azimuth))
					new_direction = SOUTHEAST;
				else if (inInterval(SR_BUFFER, WL_BUFFER, azimuth))
					new_direction = SOUTHWEST;
				else if (inInterval(WR_BUFFER, NL_BUFFER, azimuth))
					new_direction = NORTHWEST;
				else
					new_direction = old_direction;
				if (old_direction != new_direction) {
					if (new_direction == NORTH) {
						Toast.makeText(main_context, "N", Toast.LENGTH_SHORT).show();
					}
					else if (new_direction == SOUTH) {
						Toast.makeText(main_context, "S", Toast.LENGTH_SHORT).show();
					}
					else if (new_direction == EAST) {
						Toast.makeText(main_context, "E", Toast.LENGTH_SHORT).show();
					}
					else if (new_direction == WEST) {
						Toast.makeText(main_context, "W", Toast.LENGTH_SHORT).show();
					}
					else if (new_direction == NORTHEAST) {
						Toast.makeText(main_context, "NE", Toast.LENGTH_SHORT).show();
					}
					else if (new_direction == SOUTHEAST) {
						Toast.makeText(main_context, "SE", Toast.LENGTH_SHORT).show();
					}
					else if (new_direction == NORTHWEST) {
						Toast.makeText(main_context, "NW", Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(main_context, "SW", Toast.LENGTH_SHORT).show();
					}
				}
				old_direction = new_direction;
			}
		}
		new Thread(){
			@Override public void run(){
				wifi.send(new_direction);
			}
		}.start();
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {	}

	private boolean inInterval(int left, int right, int target) {
		return (target >= left && target <= right);
	}
}

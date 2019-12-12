package com.carclient;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import static android.content.Context.SENSOR_SERVICE;

public class DirectionSensor implements SensorEventListener {
    private SensorManager sensor_manager;
    private Sensor accelerometer, magnetometer;
    private Context main_context;

    private static final float EAST = 90;
    private static final float WEST = -90;
    private static final float NORTH = 0;
    private static final float SOUTH_P = 180;
    private static final float SOUTH_N = -180;
    private static final float HALF_EDGE = 8;

    private float[] gravity, geomagnetic;
    private int old_direction = 0; // 0: north, 1: east, 2: south, 3: west
    private int new_direction = 0; // 10: northeast, 30: northwest, 12 southeast, 32: southwest
    
    DirectionSensor(Context context) {
        main_context = context;
        sensor_manager = (SensorManager)main_context.getSystemService(SENSOR_SERVICE);
        accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
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
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = (float)Math.toDegrees(orientation[0]);
                if (Math.abs(azimuth - NORTH) <= HALF_EDGE)
                    new_direction = 0;
                else if (Math.abs(azimuth -WEST) <= HALF_EDGE)
                    new_direction = 3;
                else if (Math.abs(azimuth - EAST) <= HALF_EDGE)
                    new_direction = 1;
                else if (Math.abs(azimuth - SOUTH_P) <= HALF_EDGE || Math.abs(azimuth - SOUTH_N) <= HALF_EDGE)
                    new_direction = 2;
                else if (azimuth > NORTH && azimuth < EAST)
                    new_direction = 10;
                else if (azimuth > EAST && azimuth < SOUTH_P)
                    new_direction = 12;
                else if (azimuth > WEST && azimuth < NORTH)
                    new_direction = 30;
                else
                    new_direction = 32;
                if (old_direction != new_direction) {
                    if (new_direction == 0) {
                        Toast.makeText(main_context, "N", Toast.LENGTH_SHORT).show();
                    }
                    else if (new_direction == 2) {
                        Toast.makeText(main_context, "S", Toast.LENGTH_SHORT).show();
                    }
                    else if (new_direction == 1) {
                        Toast.makeText(main_context, "E", Toast.LENGTH_SHORT).show();
                    }
                    else if (new_direction == 3) {
                        Toast.makeText(main_context, "W", Toast.LENGTH_SHORT).show();
                    }
                    else if (new_direction == 10) {
                        Toast.makeText(main_context, "NE", Toast.LENGTH_SHORT).show();
                    }
                    else if (new_direction == 12) {
                        Toast.makeText(main_context, "SE", Toast.LENGTH_SHORT).show();
                    }
                    else if (new_direction == 30) {
                        Toast.makeText(main_context, "NW", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(main_context, "SW", Toast.LENGTH_SHORT).show();
                    }
                }
                old_direction = new_direction;
            }
        }

    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {	}
}

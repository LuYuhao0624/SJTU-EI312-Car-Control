package com.carclient;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import static android.content.Context.SENSOR_SERVICE;

public abstract class LightSensor implements SensorEventListener {
    private SensorManager sensor_manager;
    private Sensor light_sensor;
    private Context main_context;
    private TextView illuminance_view;
    private static final int THRESHOLD = 10;
    private static final int BUFFER = 1;

    LightSensor(Context context, TextView view) {
        main_context = context;
        sensor_manager = (SensorManager)main_context.getSystemService(SENSOR_SERVICE);
        light_sensor = sensor_manager.getDefaultSensor(Sensor.TYPE_LIGHT);
        illuminance_view = view;
    }

    void registerSensor() {
        sensor_manager.registerListener(this, light_sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    void unregisterSensor() {
        sensor_manager.unregisterListener(this);
    }
    public void onSensorChanged(SensorEvent event) {
        float illuminance = 0;
        if (event.sensor.getType() == Sensor.TYPE_LIGHT)
            illuminance = event.values[0];
        illuminance_view.setText(String.valueOf(illuminance));
        if (illuminance <= THRESHOLD-BUFFER && !isLightOn())
            lightOn();
        else if (illuminance >= THRESHOLD+BUFFER && isLightOn())
            lightOff();
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {	}
    public abstract void lightOn();
    public abstract void lightOff();
    public abstract boolean isLightOn();
}

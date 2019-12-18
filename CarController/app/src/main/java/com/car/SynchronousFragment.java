package com.car;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SynchronousFragment extends Fragment {

    private DirectionSensor direction_sensor;
    private MainActivity main_activity;
    private Context base_context;
    private Bluetooth bluetooth;
    private int old_direction = 0;
    private int new_direction = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_synchronous, container, false);
        main_activity = (MainActivity)getActivity();
        base_context = main_activity.getBaseContext();
        this.bluetooth = main_activity.bluetooth;

        TextView azimuth_view = root.findViewById(R.id.azimuth_view);
        direction_sensor = new DirectionSensor(base_context, azimuth_view) {
            @Override
            public void processDataOrSendSignal(int azimuth, int pitch, int roll) {
                decodeAzimuthOrSendSignal(azimuth);
            }
        };
        return root;
    }

    public void onResume(){
        super.onResume();
        direction_sensor.registerSensor();
    }

    public void onPause(){
        direction_sensor.unregisterSensor();
        super.onPause();
    }
    
    private void decodeAzimuthOrSendSignal(int azimuth) {
        
    }

    private int mapDegreeToSector(int azimuth) {
        // (-180, -160) -> 0, (-160, -150) -> 12;
        // (-150, -130) -> 1, (-130, -120) -> 13;
        // ...
        azimuth += 180;
        int main_sector = azimuth / 30; // from 0 to 11
        int in_buffer = (azimuth % 30) / 20; // in buffer returns 1
        return main_sector + in_buffer * 12;
    }
}
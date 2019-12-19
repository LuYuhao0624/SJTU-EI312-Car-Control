package com.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SynchronousFragment extends Fragment {

    private DirectionSensor direction_sensor;
    private Context base_context;
    private Bluetooth bluetooth;
    private int old_sector = 0;
    private int new_sector = 0;
    private int old_client_sector = 0;
    private int old_control = 0;
    private int default_control = 0;
    private TextView controller_sector_number_view, client_sector_number_view;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_synchronous, container, false);
        MainActivity main_activity = (MainActivity)getActivity();
        base_context = main_activity.getBaseContext();
        this.bluetooth = main_activity.bluetooth;

        BroadcastReceiver onOrientationArrive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int azimuth = intent.getIntExtra("data", 0);
                decodeClientAzimuthAndSendSignal(azimuth);
            }
        };
        main_activity.registerReceiver(onOrientationArrive, new IntentFilter(WifiServerThread.WIFI_ORIENTATION));

        controller_sector_number_view = root.findViewById(R.id.controller_sector_number_view);
        client_sector_number_view = root.findViewById(R.id.client_sector_number_view);
        direction_sensor = new DirectionSensor(base_context, (TextView) root.findViewById(R.id.azimuth_view)) {
            @Override
            public void processDataOrSendSignal(int azimuth, int pitch, int roll) {
                decodeControllerAzimuth(azimuth);
            }
        };

        Button forward_button = root.findViewById(R.id.forward_in_sync);
        Button stop_button = root.findViewById(R.id.stop_in_sync);
        forward_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                default_control = 1;
            }
        });
        stop_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                default_control = 0;
            }
        });
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
    
    private void decodeControllerAzimuth(int azimuth) {
        new_sector = mapDegreeToSector(azimuth);
        // if in the buffer, set new = old
        if (new_sector > 7)
            new_sector = old_sector;
        if (new_sector != old_sector) {
            controller_sector_number_view.setText(String.valueOf(new_sector));
            old_sector = new_sector;
        }
    }

    private void decodeClientAzimuthAndSendSignal(int azimuth) {
        int new_control;
        int new_client_sector = mapDegreeToSector(azimuth);
        if (new_client_sector > 7)
            new_client_sector = old_client_sector;
        if (new_client_sector != old_client_sector)
            client_sector_number_view.setText(String.valueOf(new_client_sector));
        int diff = new_sector - new_client_sector;
        if (diff == 0) {
            if (default_control == 0)
                new_control = 0;
            else
                new_control = 1;
        }
        else if ((diff + 8) % 8 < 4)
            new_control = 3; // right
        else
            new_control = 2; // left
        if (new_control != old_control) {
            bluetooth.send(new_control);
            old_control = new_control;
        }
        old_client_sector = new_client_sector;
    }

    private int mapDegreeToSector(int azimuth) {
        // (-180, -150) -> 0, (-150, -135) -> 8;
        // (-135, -105) -> 1, (-105, -90) -> 9;
        // ...
        azimuth += 180;
        int main_sector = azimuth / 45; // from 0 to 7
        int in_buffer = (azimuth % 45) / 30; // in buffer returns 1
        return main_sector + in_buffer * 8;
    }
}
package com.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class AbsoluteFragment extends Fragment {

    private static final int WEST = 0;
    private static final int NORTH = 2;
    private static final int EAST = 4;
    private static final int SOUTH = 6;
    private static final int NORTHWEST = 1;
    private static final int NORTHEAST = 3;
    private static final int SOUTHEAST = 5;
    private static final int SOUTHWEST = 7;
    private static final int STOP = -1;

    private Bluetooth bluetooth;
    private int current_direction;
    private int target_direction = -1;
    private int old_command = 0;
    private int old_direction = 0;
    private MainActivity main_activity;
    private BroadcastReceiver onOrientationArrive;
    TextView cur_dir_view, rela_dir;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_absolute, container, false);
        main_activity = (MainActivity)getActivity();
        bluetooth = main_activity.bluetooth;
        cur_dir_view = root.findViewById(R.id.cur_dir);
        rela_dir = root.findViewById(R.id.rela_dir);

        onOrientationArrive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int azimuth = intent.getIntExtra("data", 0);
                current_direction = mapDegreeToDirection(azimuth);
                switch (current_direction) {
                    case WEST:
                        cur_dir_view.setText(R.string.cur_dir_west);
                        break;
                    case NORTHWEST:
                        cur_dir_view.setText(R.string.cur_dir_northwest);
                        break;
                    case NORTH:
                        cur_dir_view.setText(R.string.cur_dir_north);
                        break;
                    case NORTHEAST:
                        cur_dir_view.setText(R.string.cur_dir_northeast);
                        break;
                    case EAST:
                        cur_dir_view.setText(R.string.cur_dir_east);
                        break;
                    case SOUTHEAST:
                        cur_dir_view.setText(R.string.cur_dir_southeast);
                        break;
                    case SOUTH:
                        cur_dir_view.setText(R.string.cur_dir_south);
                        break;
                    case SOUTHWEST:
                        cur_dir_view.setText(R.string.cur_dir_southwest);
                        break;
                }
                if (current_direction != old_direction) {
                    onChangeDirection(current_direction, target_direction);
                    old_direction = current_direction;
                }
            }
        };
        main_activity.registerReceiver(onOrientationArrive, new IntentFilter(WifiServerThread.WIFI_ORIENTATION));

        Button north = root.findViewById(R.id.button_north);
        Button south = root.findViewById(R.id.button_south);
        Button east = root.findViewById(R.id.button_east);
        Button west = root.findViewById(R.id.button_west);
        Button stop = root.findViewById(R.id.button_stop);
        north.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                target_direction = NORTH;
                onChangeDirection(current_direction, target_direction);
            }
        });
        south.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                target_direction = SOUTH;
                onChangeDirection(current_direction, target_direction);
            }
        });
        east.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                target_direction = EAST;
                onChangeDirection(current_direction, target_direction);
            }
        });
        west.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                target_direction = WEST;
                onChangeDirection(current_direction, target_direction);
            }
        });
        stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                target_direction = STOP;
                onChangeDirection(current_direction, target_direction);
            }
        });
        return root;
    }

    private void onChangeDirection(int current_dir, int target_dir) {
        int diff = target_dir - current_dir;
        int new_command;
        if (target_dir == STOP) {
            rela_dir.setText(R.string.rela_dir_stop);
            new_command = 0;
        }
        else if (diff == 0) {
            rela_dir.setText(R.string.rela_dir_forward);
            new_command = 1;
        }
        else if ((diff + 8) % 8 <= 4) {
            rela_dir.setText(R.string.rela_dir_right);
            new_command = 3;
        }
        else{
            rela_dir.setText(R.string.rela_dir_left);
            new_command = 2;
        }
        if (new_command != old_command) {
            bluetooth.send(new_command);
            old_command = new_command;
        }
    }

    private int mapDegreeToDirection(int azimuth) {
        // (165, 180) -> 0, (-180, -165) -> 0, (-165, -150) -> 8
        // (-150, -120) -> 1, (-120, -105) -> 9
        // ...
        azimuth += (180 + 15);
        int new_direction = azimuth / 45;
        int in_buffer = (azimuth % 45) / 30;
        if (in_buffer == 1)
            new_direction = old_direction;
        return new_direction;
    }

    public void onResume(){
        super.onResume();
        main_activity.registerReceiver(onOrientationArrive, new IntentFilter(WifiServerThread.WIFI_ORIENTATION));
    }

    public void onPause(){
        main_activity.unregisterReceiver(onOrientationArrive);
        super.onPause();
    }
}

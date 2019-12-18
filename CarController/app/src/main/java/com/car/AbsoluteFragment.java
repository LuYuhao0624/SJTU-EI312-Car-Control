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

    private static final int NORTH = 0;
    private static final int WEST = 2;
    private static final int SOUTH = 4;
    private static final int EAST = 6;
    private static final int NORTHWEST = 1;
    private static final int SOUTHWEST = 3;
    private static final int SOUTHEAST = 5;
    private static final int NORTHEAST = 7;
    private static final int STOP = -1;

    private Bluetooth bluetooth;
    private int current_direction;
    private int target_direction = -1;
    private int old_command = 0;
    private int new_command = 0;
    private int new_direction = 0;
    private int old_direction = 0;
    private MainActivity main_activity;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_absolute, container, false);
        main_activity = (MainActivity)getActivity();
        bluetooth = main_activity.bluetooth;

        BroadcastReceiver onOrientationArrive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int azimuth = intent.getIntExtra("data", 0);
                current_direction = convertAzimuthToDirection(azimuth);
                TextView cur_dir_view = main_activity.findViewById(R.id.cur_dir);
                if (current_direction == NORTH) {
                    cur_dir_view.setText(R.string.cur_dir_north);
                }
                else if (current_direction == SOUTH) {
                    cur_dir_view.setText(R.string.cur_dir_south);
                }
                else if (current_direction == EAST) {
                    cur_dir_view.setText(R.string.cur_dir_east);
                }
                else if (current_direction == WEST) {
                    cur_dir_view.setText(R.string.cur_dir_west);
                }
                else if (current_direction == NORTHEAST) {
                    cur_dir_view.setText(R.string.cur_dir_northeast);
                }
                else if (current_direction == SOUTHEAST) {
                    cur_dir_view.setText(R.string.cur_dir_southeast);
                }
                else if (current_direction == NORTHWEST) {
                    cur_dir_view.setText(R.string.cur_dir_northwest);
                }
                else {
                    cur_dir_view.setText(R.string.cur_dir_southwest);
                }
                onChangeDirection(current_direction, target_direction);
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
        TextView rela_dir = main_activity.findViewById(R.id.rela_dir);
        int diff = target_dir - current_dir;
        if (target_dir == STOP) {
            rela_dir.setText(R.string.rela_dir_stop);
            new_command = 0;
        }
        else if (diff == 0) {
            rela_dir.setText(R.string.rela_dir_forward);
            new_command = 1;
        }
        else if ((diff + 8) % 8 <= 4) {
            rela_dir.setText(R.string.rela_dir_left);
            new_command = 2;
        }
        else{
            rela_dir.setText(R.string.rela_dir_right);
            new_command = 3;
        }
        if (new_command != old_command) {
            bluetooth.send(new_command);
            old_command = new_command;
        }
    }

    private int convertAzimuthToDirection(int azimuth) {
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
        old_direction = new_direction;
        return new_direction;
    }

    private boolean inInterval(int left, int right, int target) {
        return (target >= left && target <= right);
    }

    private static final int BUFFER_WIDTH = 15;
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

}

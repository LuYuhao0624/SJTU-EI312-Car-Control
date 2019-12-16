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
    private MainActivity main_activity;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_absolute, container, false);
        main_activity = (MainActivity)getActivity();
        bluetooth = main_activity.bluetooth;

        BroadcastReceiver onOrientationArrive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                current_direction = intent.getIntExtra("data", 0);
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

}

package com.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Timer;
import java.util.TimerTask;

public class SpeechFragment extends Fragment {
	static char[] numbers = {'零', '一', '两', '三', '四', '五', '六', '七', '八', '九'};
	private MainActivity mainActivity;
	private Speech speech;
	private Bluetooth bluetooth;

	private Timer timer = null;
	private TimerTask task = null;

	private ImageButton btn;
	private TextView text;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_speech, container, false);

		mainActivity = (MainActivity)getActivity();
		speech = mainActivity.speech;
		bluetooth = mainActivity.bluetooth;
		btn = root.findViewById(R.id.btn_speech);
		text = root.findViewById(R.id.text_speech);

		btn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						speech.start();
						break;
					case MotionEvent.ACTION_UP:
						speech.finish();
						break;
				}
				return false;
			}
		});

		//声明语音识别数据接收器，并绑定事件
		BroadcastReceiver onRecognitionComplete = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				String result = intent.getStringExtra("result");

				if(result == null)
					return;

				text.setText(result);

				if(result.contains("停"))
					bluetooth.send(0);
				else if (result.contains("前") || result.contains("直"))
					bluetooth.send(1);
				else if (result.contains("左"))
					bluetooth.send(2);
				else if(result.contains("右"))
					bluetooth.send(3);

				int idx = result.indexOf("秒");
				if (idx >= 0) {
					char[] arr = result.toCharArray();
					int duration = 0;
					int i;
					for(i=idx-1; i>=0; i--) {
						for(int j=0; j<10; j++){
							if(arr[i] == numbers[j]){
								arr[i] = (char)('0' + j);
								break;
							}
						}
						if(!Character.isDigit(arr[i]))
							break;
					}
					for(i++; i<idx; i++)
						duration = 10*duration + arr[i] - '0';

					startTimer(duration);
				}
			}
		};
		mainActivity.registerReceiver(onRecognitionComplete, new IntentFilter(Speech.RECOGNITION_COMPLETE));

		return root;
	}

	private void startTimer(int duration){
		timer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				bluetooth.send(0);
				stopTimer();
			}
		};
		timer.schedule(task, duration*1000);
	}

	private void stopTimer(){
		timer.cancel();
		timer = null;
		task = null;
	}
}

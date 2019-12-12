package com.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

	public Bluetooth bluetooth;
	private WifiServer wifi;
	private Speech speech;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//UI相关操作
		AppBarConfiguration config = new AppBarConfiguration.Builder(
			R.id.navigation_steer, R.id.navigation_audio, R.id.navigation_gesture
		).build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		NavigationUI.setupActionBarWithNavController(this, navController, config);

		BottomNavigationView navigator = findViewById(R.id.nav_view);
		NavigationUI.setupWithNavController(navigator, navController);

		//初始化蓝牙，Wifi，以及语音识别器
		bluetooth = new Bluetooth();
		wifi = new WifiServer(getBaseContext());
		speech = new Speech(getBaseContext());

		//建立监听线程
		new WifiServerThread(getBaseContext(), wifi).start();

		//声明wifi数据接收器，并绑定事件
		BroadcastReceiver onImageArrive = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				byte[] img = intent.getByteArrayExtra("data");
				Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
				ImageView imageView = findViewById(R.id.imageView);
				imageView.setImageBitmap(bitmap);
			}
		};
		BroadcastReceiver onOrientationArrive = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				float orientation = intent.getFloatExtra("data", 0);
				//TODO: handle the event
			}
		};
		registerReceiver(onImageArrive, new IntentFilter(WifiServerThread.WIFI_IMAGE));
		registerReceiver(onImageArrive, new IntentFilter(WifiServerThread.WIFI_ORIENTATION));

		//声明语音识别数据接收器，并绑定事件
		BroadcastReceiver onRecognitionComplete = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				String result = intent.getStringExtra("result");
				TextView textview = findViewById(R.id.text_audio);
				textview.setText(result);

				int direction = 0;
				int duration = 0;
				if (result.contains("前进")) {
					direction = 1;
					bluetooth.send(1);
				} else if (result.contains("左")){
					direction = 2;
					bluetooth.send(2);
				}else if(result.contains("右")) {
					direction = 3;
					bluetooth.send(3);
				}

			}
		};
		registerReceiver(onRecognitionComplete, new IntentFilter(Speech.RECOGNITION_COMPLETE));

	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		bluetooth.disconnect();
		wifi.disconnect();
		speech.close();
	}

	public void onControlPressed(View view){
		//控制键按下时触发
		switch (view.getId()) {
			case R.id.btn_connect_bt:
				if (bluetooth.connect())
					Toast.makeText(getBaseContext(), "蓝牙连接成功", Toast.LENGTH_LONG).show();
				else
					Toast.makeText(getBaseContext(), "蓝牙连接失败", Toast.LENGTH_LONG).show();
				break;
			case R.id.btn_disconnect_bt:
				if(bluetooth.disconnect())
					Toast.makeText(getBaseContext(), "蓝牙已断开", Toast.LENGTH_LONG).show();
				else
					Toast.makeText(getBaseContext(), "蓝牙未能断开", Toast.LENGTH_LONG).show();
				break;
			case R.id.btn_connect_wifi:
				wifi.connect();  //申请wifi直连
				Toast.makeText(getBaseContext(), "正在连接", Toast.LENGTH_LONG).show();
				break;
			case R.id.btn_disconnect_wifi:
				wifi.disconnect();
				Toast.makeText(getBaseContext(), "wifi直连已断开", Toast.LENGTH_LONG).show();
				break;
		}
	}

	public void onSteerPressed(View view){
		//操纵杆有动作时触发
		switch(view.getId()){
			case R.id.btn_up:
				bluetooth.send(1);
				break;
			case R.id.btn_down:
				bluetooth.send(0);
				break;
			case R.id.btn_left:
				bluetooth.send(2);
				break;
			case R.id.btn_right:
				bluetooth.send(3);
				break;
		}
	}

	public void onAudioPressed(View view){
		//语言识别按钮按下时触发
		speech.start();
	}
}

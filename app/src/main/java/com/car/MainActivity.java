package com.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
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

	private Bluetooth bluetooth;
	private WifiServer wifi;
	private Speech speech;
    GestureDetector gesture_detector;
    protected static final float LEAST_DISPLACEMENT = 200;

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
		registerReceiver(onImageArrive, new IntentFilter(WifiServerThread.WIFI_MESSAGE));

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
		gesture_detector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {

			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				Toast.makeText(MainActivity.this, "Stop.", Toast.LENGTH_SHORT).show();
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				float x_displacement = e2.getX() - e1.getX();
				float y_displacement = e2.getY() - e1.getY();
				float abs_x_displacement = Math.abs(x_displacement);
				float abs_y_displacement = Math.abs(y_displacement);

				if (Math.max(Math.abs(x_displacement), Math.abs(y_displacement)) < LEAST_DISPLACEMENT) {
					Toast.makeText(MainActivity.this, "Move too short. Try a longer move.", Toast.LENGTH_SHORT).show();
					return false;
				}

				if (abs_x_displacement > abs_y_displacement) {
					if (x_displacement > 0) {
						Toast.makeText(MainActivity.this, "Turn right.", Toast.LENGTH_SHORT).show();
						// turn right
						bluetooth.send(3);
					}
					else {
						Toast.makeText(MainActivity.this, "Turn left.", Toast.LENGTH_SHORT).show();
						// turn left
						bluetooth.send(2);
					}
					return true;
				}
				else if (abs_x_displacement < abs_y_displacement) {
					if (y_displacement > 0) {
						Toast.makeText(MainActivity.this, "Stop.", Toast.LENGTH_SHORT).show();
						// stop
						bluetooth.send(0);
					}
					else {
						Toast.makeText(MainActivity.this, "Move forward.", Toast.LENGTH_SHORT).show();
						// forward
						bluetooth.send(1);
					}
					return true;
				}
				else {
					Toast.makeText(MainActivity.this, "Move distance too close on two axises.", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		});

	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		bluetooth.disconncet();
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
				if(bluetooth.disconncet())
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
		NavController navi_controller = Navigation.findNavController(this, R.id.nav_host_fragment);
		// get the current current navigation destination id
		int navi_id = navi_controller.getCurrentDestination().getId();
		// compare got id with that of navigation_gesture fragment id
		if (navi_id == R.id.navigation_gesture) {
			return gesture_detector.onTouchEvent(event);
		}
		return false;
    }
}

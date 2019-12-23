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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

	public Bluetooth bluetooth;
	public WifiServer wifi;
	public Speech speech;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//UI相关操作
		AppBarConfiguration config = new AppBarConfiguration.Builder(
				R.id.navigation_speech, R.id.navigation_gesture, R.id.navigation_gravity,
				R.id.navigation_absolute, R.id.navigation_synchronous
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
		registerReceiver(onImageArrive, new IntentFilter(WifiServerThread.WIFI_IMAGE));
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
}

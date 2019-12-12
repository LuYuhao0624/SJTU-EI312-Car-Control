package com.carclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;

public class MainActivity extends AppCompatActivity{
	private WifiClient wifi;
	private CameraPreview cameraPreview;
	private DirectionSensor direction_sensor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//开启Wifi客户端
		wifi = new WifiClient(getBaseContext());

		//构造preview对象。它能集中处理与预览相关的事件，并且在新帧到来时，将帧压缩并用wifi发送。
		cameraPreview = new CameraPreview(getBaseContext(), wifi);

		// start direction sensor
		direction_sensor = new DirectionSensor(getBaseContext());

		//把preview对象与主界面的surfaceView组件绑定
		SurfaceView view = findViewById(R.id.surfaceView);
		view.getHolder().addCallback(cameraPreview);
	}

	@Override
	protected void onResume() {
		super.onResume();
		direction_sensor.registerSensor();
	}

	@Override
	protected void onPause() {
		direction_sensor.unregisterSensor();
		super.onPause();
	}

	@Override
	protected void onDestroy(){
		wifi.disconnect();
		cameraPreview.close();
		super.onDestroy();
	}

	public void onConnectPressed(View view){
		wifi.connect();
	}

	public void onDisconnectPressed(View view){
		wifi.disconnect();
	}
}

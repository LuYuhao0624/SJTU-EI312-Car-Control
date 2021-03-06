package com.carclient;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{
	private WifiClient wifi;
	private CameraPreview cameraPreview;
	private DirectionSensor direction_sensor;
	private LightSensor light_sensor;
	private boolean direction_on = false;
	private boolean auto_light_on = false;
	private boolean light_on = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//开启Wifi客户端
		wifi = new WifiClient(getBaseContext());

		//构造preview对象。它能集中处理与预览相关的事件，并且在新帧到来时，将帧压缩并用wifi发送。
		cameraPreview = new CameraPreview(getBaseContext(), wifi);

		TextView azimuth_view = findViewById(R.id.azimuthView);
		TextView illuminance_view = findViewById(R.id.illuminanceView);

		// start direction sensor
		direction_sensor = new DirectionSensor(getBaseContext(), wifi, azimuth_view);

		light_sensor = new LightSensor(getBaseContext(), illuminance_view) {
			@Override
			public void lightOn() {
				turnOnLight();
			}

			@Override
			public void lightOff() {
				turnOffLight();
			}

			@Override
			public boolean isLightOn() {
				return light_on;
			}
		};

		//把preview对象与主界面的surfaceView组件绑定
		SurfaceView view = findViewById(R.id.surfaceView);
		view.getHolder().addCallback(cameraPreview);
	}

	@Override
	protected void onDestroy(){
		wifi.disconnect();
		cameraPreview.close();
		if (direction_on)
			direction_sensor.unregisterSensor();
		light_sensor.unregisterSensor();
		super.onDestroy();
	}

	public void onFlipDirection(View view) {
		TextView direction_status = findViewById(R.id.direction_switch);
		TextView azimuth_view = findViewById(R.id.azimuthView);
		if (direction_on) {
			direction_sensor.unregisterSensor();
			direction_status.setText(R.string.off);
			azimuth_view.setText("Azimuth");
		}
		else {
			direction_sensor.registerSensor();
			direction_status.setText(R.string.on);
		}
		direction_on = !direction_on;
	}

	public void onFlipAutoLight(View view) {
		TextView auto_light_status = findViewById(R.id.auto_light_status);
		TextView illuminance_view = findViewById(R.id.illuminanceView);
		auto_light_on = !auto_light_on;
		if (auto_light_on) {
			light_sensor.registerSensor();
			auto_light_status.setText(R.string.on);
		}
		else {
			light_sensor.unregisterSensor();
			auto_light_status.setText(R.string.off);
			illuminance_view.setText("Illuminance");
		}
	}

	public void turnOnLight() {
		cameraPreview.setFlashLight(true);
		light_on = true;
		Toast.makeText(this, "light_on:" + String.valueOf(light_on), Toast.LENGTH_SHORT).show();
	}

	public void turnOffLight() {
		cameraPreview.setFlashLight(false);
		light_on = false;
		Toast.makeText(this, "light_on:" + String.valueOf(light_on), Toast.LENGTH_SHORT).show();
	}

	public void onConnectPressed(View view){
		wifi.connect();
	}

	public void onDisconnectPressed(View view){
		wifi.disconnect();
	}

}

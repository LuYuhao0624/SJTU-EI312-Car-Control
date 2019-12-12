package com.carclient;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
	private WifiClient wifi;
	private CameraPreview cameraPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//开启Wifi客户端
		wifi = new WifiClient(getBaseContext());

		//构造preview对象。它能集中处理与预览相关的事件，并且在新帧到来时，将帧压缩并用wifi发送。
		cameraPreview = new CameraPreview(getBaseContext(), wifi);

		//把preview对象与主界面的surfaceView组件绑定
		SurfaceView view = findViewById(R.id.surfaceView);
		view.getHolder().addCallback(cameraPreview);
	}

	@Override
	protected  void onDestroy(){
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

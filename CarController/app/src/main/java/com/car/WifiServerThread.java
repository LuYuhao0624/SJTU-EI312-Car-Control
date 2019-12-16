package com.car;

import android.content.Context;
import android.content.Intent;

import java.util.Arrays;

public class WifiServerThread extends Thread{
	static final String WIFI_IMAGE = "wifi_image";
	static final String WIFI_ORIENTATION = "wifi_orientation";
	static final byte TYPE_INT = 0;
	static final byte TYPE_BYTEARRAY = 1;
	private WifiServer server;
	private Context context;

	WifiServerThread(Context context, WifiServer server){
		this.context = context;
		this.server = server;
	}

	@Override
	public void run(){
		byte[] data;
		Intent intent;
		int len;

		while(true) {
			data = server.receive();
			if(data == null)
				continue;

			len = data.length;
			if(data[len-1] == TYPE_INT) {  //收到朝向
				int tmp = (0xff000000 & (data[0] << 24)) | (0x00ff0000 & (data[1] << 16)) |
						(0x0000ff00 & (data[2] << 8)) | (0x000000ff & data[3]);
				intent = new Intent(WIFI_ORIENTATION);
				intent.putExtra("data", tmp);
				context.sendBroadcast(intent);
			}else if(data[len-1] == TYPE_BYTEARRAY) {  //收到图像
				intent = new Intent(WIFI_IMAGE);
				intent.putExtra("data", Arrays.copyOf(data, len - 1));
				context.sendBroadcast(intent);
			}
		}
	}
}

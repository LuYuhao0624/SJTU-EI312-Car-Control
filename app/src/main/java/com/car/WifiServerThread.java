package com.car;

import android.content.Context;
import android.content.Intent;

public class WifiServerThread extends Thread{
	public static String WIFI_MESSAGE = "wifi_message";
	private WifiServer server;
	private Context context;

	WifiServerThread(Context context, WifiServer server){
		this.context = context;
		this.server = server;
	}

	@Override
	public void run(){
		Intent intent = new Intent(WIFI_MESSAGE);
		while(true) {
			byte[] img = server.receive();
			if(img == null)
				continue;

			intent.putExtra("data", img);
			context.sendBroadcast(intent);
		}
	}
}

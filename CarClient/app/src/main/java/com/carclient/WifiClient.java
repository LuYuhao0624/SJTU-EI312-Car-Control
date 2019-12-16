package com.carclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WifiClient {
	static byte TYPE_INT = 0;
	static byte TYPE_BYTEARRAY = 1;
	private String TAG = "Swallow.wifi";
	private String SERVER_MAC = "52:04:b8:42:0f:01";

	private WifiP2pManager wifiManager;
	private WifiP2pManager.Channel wifiChannel;

	private InetAddress serverAddress;
	private DatagramSocket socket;

	WifiClient(Context context){
		//初始化点对点通信框架
		wifiManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		wifiChannel = wifiManager.initialize(context, context.getMainLooper(), null);

		//定义事件监听器
		BroadcastReceiver eventHandler = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

				if(wifiInfo != null && wifiInfo.groupFormed) {
					serverAddress = wifiInfo.groupOwnerAddress;
					wifiManager.stopPeerDiscovery(wifiChannel, null);
					Toast.makeText(context, "连接成功。服务端IP = " + serverAddress.getHostAddress(), Toast.LENGTH_LONG).show();
				}
			}
		};

		//定义事件筛选器，并将其与事件监听器绑定
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		context.registerReceiver(eventHandler, intentFilter);

		//初始化socket。发送端口设为2504。
		try {
			socket = new DatagramSocket(2504);
			Log.i(TAG, "Socket opened.");
		}catch (Exception e){
			Log.e(TAG, "Socket open failed.");
		}
	}

	public void connect(){
		//开始发现Peers
		wifiManager.discoverPeers(wifiChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess(){
				Log.i(TAG, "Peer discovery initiated.");
			}
			@Override
			public void onFailure(int reason){
				Log.e(TAG, "Peer discovery initiation failed.");
			}
		});
	}

	public void disconnect(){
		wifiManager.stopPeerDiscovery(wifiChannel, null);
		wifiManager.cancelConnect(wifiChannel, null);
		socket.close();
	}

	public boolean send(byte[] data){
		//UDP通信
		if(serverAddress != null) {
			DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress,2504);
			try {
				socket.send(packet);
			} catch (Exception e) {
				Log.e(TAG, "Transmission failed.");
				return false;
			}
		}
		return true;
	}

	public boolean send(int data){
		//wrapper
		byte[] byteArray = new byte[5];  //The final byte indicates the type of data

		byteArray[0] = (byte)((data & 0xff000000) >> 24);
		byteArray[1] = (byte)((data & 0x00ff0000) >> 16);
		byteArray[2] = (byte)((data & 0x0000ff00) >> 8);
		byteArray[3] = (byte)(data & 0x000000ff);
		byteArray[4] = WifiClient.TYPE_INT;

		return send(byteArray);
	}
}

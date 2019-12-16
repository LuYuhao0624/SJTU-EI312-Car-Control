package com.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collection;

public class WifiServer {
	//private static String CLIENT_MAC = "02:7d:3d:ed:23:31";
	private static String CLIENT_MAC = "56:25:ea:f3:19:df";
	private static String TAG = "Swallow.wifi";

	private WifiP2pManager wifiManager;
	private WifiP2pManager.Channel wifiChannel;
	private WifiP2pConfig wifiConfig;

	private InetAddress serverAddress;
	private DatagramSocket socket;

	WifiServer(Context context){
		//初始化点对点通信框架
		wifiManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		wifiChannel = wifiManager.initialize(context, context.getMainLooper(), null);
		wifiConfig = new WifiP2pConfig();
		wifiConfig.deviceAddress = CLIENT_MAC;
		wifiConfig.groupOwnerIntent = 15;  //极需成为组长
		wifiConfig.wps.setup = WpsInfo.PBC;

		//定义事件监听器
		BroadcastReceiver eventHandler = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch(intent.getAction()){
					case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
						//周边设备有变
						WifiP2pDeviceList list = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
						WifiP2pDevice wifiClient = list.get(CLIENT_MAC);

						if (wifiClient != null && wifiClient.status == WifiP2pDevice.AVAILABLE) {
							//客户端被找到且可用
							Log.i(TAG, "Client found");
							Log.i(TAG, "address: " + wifiClient.deviceAddress);

							//建立p2p信道
							wifiManager.connect(wifiChannel, wifiConfig, new WifiP2pManager.ActionListener() {
								@Override
								public void onSuccess() {
									Log.i(TAG, "Connection request sent.");
								}
								@Override
								public void onFailure(int reason) {
									Log.e(TAG, "Connection request sending failed.");
								}
							});
						}
						/*
						Collection<WifiP2pDevice> l = list.getDeviceList();
						for(WifiP2pDevice peer: l) {
							Log.i(TAG, peer.deviceAddress);
						}*/
						break;

					case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
						//连接状态有变
						WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
						if(wifiInfo.isGroupOwner)
							Log.i(TAG, "owner!");
						if(wifiInfo.groupFormed  && wifiInfo.isGroupOwner) {
							Log.i(TAG, "Connection established.");
							serverAddress = wifiInfo.groupOwnerAddress;
							wifiManager.stopPeerDiscovery(wifiChannel, null);
							Toast.makeText(context, "连接成功。本地IP = " + serverAddress.getHostAddress(), Toast.LENGTH_LONG).show();
						}
						break;
				}
			}
		};

		//定义事件筛选器，并将其与事件监听器绑定
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		context.registerReceiver(eventHandler, intentFilter);

		//初始化socket，监听端口2504
		try {
			socket = new DatagramSocket(2504);
			Log.i(TAG, "Socket created.");
		} catch (Exception e){
			Log.e(TAG, "Socket creation failed.");
		}
	}

	public void connect(){
		//发现Peers
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

	public byte[] receive(){
		byte[] buffer = new byte[65536];  //数据包最大为64KB
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		if(!socket.isClosed()){
			try {
				socket.receive(packet);
				byte[] ret = new byte[packet.getLength()];
				System.arraycopy(buffer, 0, ret, 0, packet.getLength());
				return ret;
			} catch (Exception e) {
				Log.e(TAG, "Packet reception failed.");
			}
		}
		return null;
	}
}
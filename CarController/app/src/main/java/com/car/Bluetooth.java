package com.car;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class Bluetooth {
	private String TAG = "Swallow.bluetooth";

	private BluetoothAdapter btAdapter;  //本地蓝牙适配器
	private BluetoothDevice btDevice;  //远程蓝牙设备
	private BluetoothSocket btSocket;  //蓝牙信道
	private OutputStream btStream;  //蓝牙信道上的数据流

	Bluetooth(){
		//获取蓝牙适配器
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter == null) {
			Log.e(TAG, "Bluetooth not supported.");
			return;
		}

		//开启蓝牙
		if(!btAdapter.isEnabled())
			btAdapter.enable();

		//关闭搜索
		if(btAdapter.isDiscovering())
			btAdapter.cancelDiscovery();

		//根据小车蓝牙的MAC地址找到小车（MAC地址是通过getBondedDevice()查询得到的）
		btDevice = btAdapter.getRemoteDevice("00:19:09:11:1F:D4");
		if(btDevice == null){
			Log.e(TAG, "Bond the car and retry.");
			return;
		}
		Log.i(TAG, "Car bonded");
	}

	public boolean connect(){
		//打开信道
		try{
			btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			if(btSocket != null){
				btSocket.connect();
				btStream = btSocket.getOutputStream();
			}
		}catch(IOException e){
			Log.e(TAG, "Car not detected.");
			return false;
		}
		Log.i(TAG, "Connection established.");
		return true;
	}

	public boolean disconnect(){
		//关闭信道
		if(btSocket == null)
			return true;
		try{
			btSocket.close();
		}catch(IOException e){
			Log.e(TAG, "Fail to close the connection.");
			return false;
		}
		Log.i(TAG, "Connection closed.");
		return true;

	}

	public boolean isConnected(){
		return (btSocket != null && btSocket.isConnected());
	}

	public void send(int command){
		if(btSocket!=null && btSocket.isConnected()) {
			try{
				btStream.write(command);
				btStream.flush();
			}catch(IOException e){
				Log.e(TAG, "Transmission Error");
			}
		}
	}
}

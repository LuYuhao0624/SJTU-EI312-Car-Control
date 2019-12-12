package com.carclient;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private static String TAG = "Swallow.camera";
	private static int quality = 80;
	private static int width = 640, height = 480;
	//Supported: (1280,960), (1280,720), (960,720), (720,540), (720,480), (640,480), (576,432), (480,320)

	private Camera camera;
	private WifiClient wifi;

	public CameraPreview(Context context, WifiClient wifi) {
		super(context);
		this.wifi = wifi;

		//申请占用相机
		try{
			this.camera = Camera.open();
			Log.i(TAG, "Camera connected.");
		}catch(Exception e){
			Log.e(TAG, "Camera is in use.");
		}

		//调整相机参数
		camera.setDisplayOrientation(90);
		Camera.Parameters param = camera.getParameters();
		param.setPreviewSize(width, height);
		camera.setParameters(param);

		//绑定相机与自己，从而自己能监听到相机传来的新帧
		camera.setPreviewCallback(this);  //camera预览的回调函数由onPreviewFrame实现
	}

	public void close(){
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//界面创建成功时运行
		Log.i(TAG, "Surface created.");
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
			Log.e(TAG, "Failed to set camera preview.");
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		//界面行将销毁时运行
		;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		//界面发生改变时运行
		;
	}

	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		//新的一帧来到
		if(data == null)
			return;

		new Thread(){
			@Override public void run(){
				YuvImage img = new YuvImage(data, ImageFormat.NV21, width, height, null);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				img.compressToJpeg(new Rect(0, 0, width, height), quality, stream);
				try{
					stream.flush();
					wifi.send(stream.toByteArray());
					stream.close();
				}catch(IOException e){
					Log.e(TAG, "Failed to flush the IOStream");
				}
			}
		}.start();
	}
}

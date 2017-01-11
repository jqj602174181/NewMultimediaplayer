package com.centerm.mediaplayer;

import java.util.ArrayList;

import com.centerm.mediaplayer.three.MenuFxService;
import com.centerm.mediaplayer.three.MenuFxService.MyBinder;


import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;
import android.util.LruCache;

public class MyApp extends Application{

	private static MyApp instance;

	public ArrayList<CommonActivity> list = new ArrayList<CommonActivity>();

	public MyServiceConnection serviceConn;

	public LruCache<String, Bitmap> lruCache;

	@Override
	public void onCreate() {
		super.onCreate();

		instance = this;

		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024); 
		// ʹ���������ڴ�ֵ��1/4��Ϊ����Ĵ�С�� 
		int cacheSize = maxMemory / 8; 
		lruCache = new LruCache<String, Bitmap>(cacheSize) { 
			@Override
			protected int sizeOf(String key, Bitmap bitmap) { 
				// ��д�˷���������ÿ��ͼƬ�Ĵ�С��Ĭ�Ϸ���ͼƬ������ 
				return bitmap.getByteCount() / 1024; 
			} 
		}; 

		serviceConn = new MyServiceConnection();
		serviceConn.bindService();
	};

	public static MyApp getInstance(){
		return instance;
	}
	
	public void cleanLruCache(){
		if(lruCache != null){
			Log.e("TAG", "msg:" + lruCache.size());
		}
	}

	public class MyServiceConnection implements ServiceConnection{

		boolean flag = false;
		MenuFxService menuFxService;

		public MyServiceConnection(){
		}

		private void bindService(){
			Intent intent = new Intent(MyApp.this, MenuFxService.class);
			MyApp.this.bindService(intent, this, Context.BIND_AUTO_CREATE);
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MyBinder binder = (MyBinder)service;
			menuFxService = binder.getService();
			flag = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			flag = false;
		}

		public void closeFloatView(){
			if(flag){
				menuFxService.closeFloatView();
			}
		}
	}
}

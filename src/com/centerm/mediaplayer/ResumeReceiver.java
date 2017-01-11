package com.centerm.mediaplayer;

import com.centerm.mediaplayer.second.NewPlayController;
import com.centerm.mediaplayer.second.NewPlayControllerEX;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ResumeReceiver extends BroadcastReceiver
{
	private final String TAG = "receiver";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.v(TAG, "intent" + intent);
		
		// 获取上下文
		Intent startIntent = new Intent();
		startIntent.setClass(MainActivity.getProcessContext(), NewPlayController.class);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainActivity.getProcessContext().startActivity(startIntent);
	}
}

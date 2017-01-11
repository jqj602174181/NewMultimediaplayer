package com.centerm.mediaplayer;

import android.app.Activity;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.KeyEvent;

/**
 * 集成按键处理、声音处理，供其它Activity来继承
 */
public class CommonActivity extends Activity {
	
	public final static int KEY_COMB = 138;//组合键
	private int sndid = -1;					//声音文件ID
	SoundPool sndPool = null;				//声音池对象
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		initKeySound();//加载按键声音
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		freeSound();//释放声音资源
	}
	
	/**
	 * 加载按键声音
	 */
	private void initKeySound()
	{
		sndPool = new SoundPool(16, AudioManager.STREAM_ALARM, 0);
		if( sndPool != null )
		{
			sndid = sndPool.load(this, R.raw.beep, 1);
		}
	}
	
	/**
	 * 释放声音资源
	 */
	private void freeSound()
	{
		if (sndPool != null)
		{
			sndPool.release();
			sndPool = null;
		}
	}
	
	/**
	 * 播放按键音
	 */
	public void playKeySound()
	{
		float fSoundVolume = 1.0f;//采用音量
		
		//得到最大音量和当前音量值，然后计算得到音量百分比
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if( audioManager != null )
		{
			int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
			int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
			fSoundVolume = (float) currentVol / (float) maxVol;
		}//else 采用默认最大声音
		
		//播放声音
		if( sndPool != null )
		{
			sndPool.play(sndid, fSoundVolume, fSoundVolume, 1, 0, (float) 1.0);
		}//else 不要播放声音		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.i("media", String.format("onkeydown,keycode=%d,%d",keyCode, KeyEvent.KEYCODE_F2));
		//收到组合键处理，则启动设置界面
		if (KEY_COMB == keyCode)
		{
			playKeySound();

			// 发送广播
			final String ACTION = "com.centerm.media.START_APP_ACTION";
			Intent intent = new Intent(); // 实例化Intent
			intent.setAction(ACTION);
			sendBroadcast(intent);
			Log.i(MainActivity.TAG, "send broadcast");
		}
		//return super.onKeyDown(keyCode, event);
		//Log.i("media", "return true");
		return true;
	}
}

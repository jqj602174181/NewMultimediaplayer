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
 * ���ɰ���������������������Activity���̳�
 */
public class CommonActivity extends Activity {
	
	public final static int KEY_COMB = 138;//��ϼ�
	private int sndid = -1;					//�����ļ�ID
	SoundPool sndPool = null;				//�����ض���
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		initKeySound();//���ذ�������
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		freeSound();//�ͷ�������Դ
	}
	
	/**
	 * ���ذ�������
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
	 * �ͷ�������Դ
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
	 * ���Ű�����
	 */
	public void playKeySound()
	{
		float fSoundVolume = 1.0f;//��������
		
		//�õ���������͵�ǰ����ֵ��Ȼ�����õ������ٷֱ�
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if( audioManager != null )
		{
			int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
			int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
			fSoundVolume = (float) currentVol / (float) maxVol;
		}//else ����Ĭ���������
		
		//��������
		if( sndPool != null )
		{
			sndPool.play(sndid, fSoundVolume, fSoundVolume, 1, 0, (float) 1.0);
		}//else ��Ҫ��������		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.i("media", String.format("onkeydown,keycode=%d,%d",keyCode, KeyEvent.KEYCODE_F2));
		//�յ���ϼ��������������ý���
		if (KEY_COMB == keyCode)
		{
			playKeySound();

			// ���͹㲥
			final String ACTION = "com.centerm.media.START_APP_ACTION";
			Intent intent = new Intent(); // ʵ����Intent
			intent.setAction(ACTION);
			sendBroadcast(intent);
			Log.i(MainActivity.TAG, "send broadcast");
		}
		//return super.onKeyDown(keyCode, event);
		//Log.i("media", "return true");
		return true;
	}
}

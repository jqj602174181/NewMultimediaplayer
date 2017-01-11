package com.centerm.mediaplayer;

import com.centerm.mediaplayer.ipc.MessageChannel;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class MainActivity extends Activity {
	public static final String TAG = "mediaplayer";				//���ּ��̳����log��ǩ
	public static final int ID = 10;							//mediaplayer�����id
	private static Application application;					//Ӧ�ó���������
	private static MessageChannel messageChannel = null;	//��Ϣ�ܵ�
	public Handler handler  = new Handler();

	
	/**
	 * ��ȡ������ص������Ķ���
	 * @return ������ص������Ķ���
	 */
	public static Application getProcessContext()
	{
		return application;
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//�����߳���Ϣ����ת����MainLoopHandler���д��� ��������������Ϣ
		if( messageChannel == null )//ֻ����һʵ������
		{
			application = getApplication();//�õ�������ص�������
			
			Intent intent = getIntent();
			int flowNo = intent.getIntExtra( "FlowNO", 1 );
			messageChannel = MessageChannel.getInstance();//����ͨ�Źܵ��߳�
			messageChannel.createChannel( flowNo );//��������ȵ�����
		}
		else//���ȳ���ֻ����һ��ʵ�������У���Ӧ�ó��ֶ������ʵ��Ĵ���
		{
			Log.e( TAG, "Can't create two app instance!" );
		}
		finish();//����activity
	}

}

package com.centerm.mediaplayer.ipc;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.centerm.mediaplayer.PlayController;
import com.centerm.mediaplayer.MainActivity;
import com.centerm.mediaplayer.second.NewPlayController;
import com.centerm.mediaplayer.second.NewPlayControllerEX;

/**
 * �������Ե��ȵ�������Ϣ
 */
public class MessageHandler extends Handler{
	
	//ִ�н��
	public static final int RESULT_OK = 0;			//ִ�гɹ�
	public static final int RESULT_ERR = 1;			//ִ��ʧ��
	
	/**
	 * ���캯���� ��handler����looper
	 */
	public MessageHandler( Looper looper )
	{
		super( looper );
	}

	
	@Override
	public void handleMessage(Message msg) {
		Log.i( MainActivity.TAG, "handleMessage:msg.what=" + msg.what);
		switch( msg.what )
		{
			case MessageType.MSG_START://����Ӧ�ó���
				Context context = MainActivity.getProcessContext();//��ȡ������
				
				//������ý�岥�ſ���Activity
				Intent intent = new Intent();
				intent.setClass( context, NewPlayController.class );
				intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				context.startActivity( intent );//����activity
				sendNoticeResultMessage(MessageType.MSG_NOTICE_STATE_RUN, MainActivity.ID, msg.arg1);//���ͳɹ�
				break;
			default:
				super.handleMessage(msg);
				//TODO:log
				break;
		}
	}
	
	/**
	 * ����֪ͨ��Ϣ�����ȣ�˵��һ�����������
	 * @param nProgramID ����ID
	 * @param flowNo ��ˮ��
	 */
	private void sendNoticeResultMessage( int msgType, int nProgramID, int flowNo )
	{
		Message msg = Message.obtain();
		msg.what = msgType;
		msg.arg1 = nProgramID;
		msg.arg2 = flowNo;
		MessageChannel.getInstance().sendMessage( msg );
	}
}

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
 * 处理来自调度的命令消息
 */
public class MessageHandler extends Handler{
	
	//执行结果
	public static final int RESULT_OK = 0;			//执行成功
	public static final int RESULT_ERR = 1;			//执行失败
	
	/**
	 * 构造函数， 将handler绑定至looper
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
			case MessageType.MSG_START://启动应用程序
				Context context = MainActivity.getProcessContext();//获取上下文
				
				//启动多媒体播放控制Activity
				Intent intent = new Intent();
				intent.setClass( context, NewPlayController.class );
				intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				context.startActivity( intent );//启动activity
				sendNoticeResultMessage(MessageType.MSG_NOTICE_STATE_RUN, MainActivity.ID, msg.arg1);//发送成功
				break;
			default:
				super.handleMessage(msg);
				//TODO:log
				break;
		}
	}
	
	/**
	 * 发送通知消息给调度，说明一个操作已完成
	 * @param nProgramID 程序ID
	 * @param flowNo 流水号
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

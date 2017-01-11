package com.centerm.mediaplayer.ipc;

import com.centerm.mediaplayer.MainActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Messenger;
import android.os.Message;
import android.util.Log;

/**
 * 与调度间的消息通道, 采用单一实例实现
 */
public class MessageChannel {

	public final static String THREADNAME = MainActivity.TAG + "_MessageChannel";//线程名称
	public final static String DISPATCHCLS = "com.centerm.dispath.ipc.MessageService";//调度通信服务的类的全限定名
	private Messenger localMsger;		//应用程序本地的Messenger
	private Messenger remoteMsger;		//调度程序的Messenger
	private Handler handler;			//服务的handler
	private HandlerThread thread;		//消息处理线程
	private static MessageChannel channel;//单一实例
	private int flowNo;					//调度内部Action的流水号
	
	private MessageChannel()
	{
		thread = new HandlerThread( THREADNAME );//创建消息处理线程
		thread.start();
		handler = new MessageHandler( thread.getLooper() );
		localMsger = new Messenger( handler );//创建本地的Messenger
	}
	
	public static MessageChannel getInstance()
	{
		if( channel == null )
		{
			channel = new MessageChannel();//单一实例
		}
		return channel;
	}
	
	/**
	 * 创建与调度的通信通道
	 * @param flowNo 调度要求回馈的流水号
	 */
	public void createChannel( int flowNo )
	{
		this.flowNo = flowNo;
		Intent intent = new Intent( DISPATCHCLS );
		MainActivity.getProcessContext().bindService( intent, rmServiceConn, Context.BIND_AUTO_CREATE );	
	}
	
	/**
	 * 发送消息给调度程序
	 * @param msg 预发送的消息
	 * @return true表示成功，false表示失败
	 */
	public boolean sendMessage( Message msg )
	{
		try
		{
			msg.replyTo = localMsger;
			remoteMsger.send( msg );
			return true;
		}catch( Exception e )
		{
			//TODO:log
			return false;
		}
	}
	
	//远程服务连接
	private ServiceConnection rmServiceConn = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName name, IBinder service) {
			remoteMsger = new Messenger( service );
			Log.i( MainActivity.TAG, "Dispatch Service is connected!");
			
			//通知调度，可以通过给定的messenger来与本程序通信
			Message msg = Message.obtain();
			msg.what = MessageType.MSG_CONTACTME;
			msg.arg1 = MainActivity.ID;
			msg.arg2 = flowNo;
			sendMessage( msg );
		}

		public void onServiceDisconnected(ComponentName name) {
			remoteMsger = null;
		}
	};
}

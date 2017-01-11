package com.centerm.mediaplayer.ipc;

/*
 * 定义调度与各个应用程序间的通用消息类型
 */
public class MessageType {

	 
	/*
	 * 1 - 100 为调度至应用程序方向的消息
	 * 101 - 102为应用程序至调度方向的消息
	 */
	public final static int MSG_START = 3;				  //启动应用程序的界面，arg1是流水号，obj是指令数据
	public final static int MSG_CLOSE = 4;				  //关闭应用程序的界面，arg1是流水号，obj是指令数据
	public final static int MSG_EXIT = 5;                 //结束应用程序，arg1是流水号，obj是指令数据
	public final static int MSG_DATA = 6;                 //处理应用程序设置指令，arg1是流水号， obj是指令数据
	public final static int MSG_CONTACTME = 101;		  //表示"以后保持联系"。此时，应用程序主动告诉调度，应用程序的通信Messenger, arg1表示应用程序id，arg2表示Action的流水号
	public final static int MSG_FORGETME = 102;			  //应用程序主动断开调度的通信服务,通常只有在应用程序退出时发送该消息, arg1表示应用程序id
	public final static int MSG_SEND_DATA = 103;		  //发送数据给主机，arg1表示应用程序id，obj表示上传主机的数据
	public final static int MSG_NOTICE_OPERATOR_END = 111;//通知界面操作执行后程序状态,arg1表示应用程序id，arg2为执行应用程序状态，obj表示发送给主机的数据	
	public final static int MSG_NOTICE_STATE_DATA = 112;  //通知调度，应用程序指令操作设置成功，arg1表示应用程序id，arg2表示Action的流水号，obj表示上传主机的数据
	public final static int MSG_NOTICE_STATE_CLOSE = 113; //通知调度，应用程序处于后台运行状态，arg1表示应用程序id，arg2表示Action的流水号
	public final static int MSG_NOTICE_STATE_RUN = 114;   //通知调度，应用程序处于运行状态，arg1表示应用程序id，arg2表示Action的流水号
}
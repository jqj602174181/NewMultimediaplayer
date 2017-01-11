package com.centerm.mediaplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

import android.util.Log;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.centerm.commons.utils.FileUtils;
import com.centerm.mediaplayer.image.ImagePlayer;
import com.centerm.mediaplayer.image.ImagePlayerEx;
import com.centerm.mediaplayer.video.VideoPlayer;

/**
 * 遍历获取多媒体资源文件，然后控制采用哪个Acitivity或App来执行播放。
 * 
 */
public class PlayController extends CommonActivity
{
	// 静态变量定义
	//public static final String IMAGE_PATH = "/vendor/media/picture"; // 图片文件所在的目录
	//public static final String VIDEO_PATH = "/vendor/media/video"; // 视频文件所在的目录
	public static final String IMAGE_PATH = "/mnt/internal_sd/media/picture"; // 图片文件所在的目录
	public static final String VIDEO_PATH = "/mnt/internal_sd/media/video"; // 视频文件所在的目录
	public static final int FILE_TYPE_IMAGE = 0; // 图片文件类别
	public static final int FILE_TYPE_VIDEO = 1; // 视频文件类别
	public static final int FILE_TYPE_VIDEO_ACL = 2; // android已实现加速的视频文件类别
	public static final int REQUEST_CODE_PLAY_IMAGE = 0; // 播放图片的请求
	public static final int REQUEST_CODE_PLAY_VIDEO = 1; // 播放视频的请求
	public static final int REQUEST_CODE_PLAY_VIDEO_ACL = 2; // 播放可加速的视频的请求
	public static final String supportedImageFormats[] = { "gif", "bmp", "png",
			"jpg" };// 支持的图片文件格式
	public static final String supportedVideoFormats[] = // 支持的视频格式
	{ /* "mp4", */"wmv", "avi", "mkv", "rm", "mpg", "mpeg", "flv", "divx", "swf",
			"dat", "h264", "h263", "h261", "3gp", "3gpp", "asf", "mov", "m4v",
			"ogv", "vob", "vstream", "ts", "webm", "vro", "tts", "tod", "rmvb",
			"rec", "ps", "ogx", "ogm", "nuv", "nsv", "mxf", "mts", "mpv2",
			"mpeg1", "mpeg2", "mpeg4", "mpe", "mp4v", "mp2v", "mp2", "m2ts",
			"m2t", "m2v", "m1v", "amv", "3gp2" };
	public static final String supportedAccelerateVideoFormats[] = { "mp4" };// 硬件或软件已实现加速处理的视频格式
	public static final int EVENT_REDETECT = 1; // 若播放资源为空，需要重新检测
	public static final int INTERVAL_TIME_REDETECT = 10; // 若播放资源为空，每10秒钟检测一次
	private boolean bPlayLoop = false; // 循环播放
	private static final String TAG = MainActivity.TAG;
	
	// 变量
	LinkedList<Task> taskList = null; // 任务列表

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(MainActivity.TAG, "playcontroller start");
		super.onCreate(savedInstanceState);
		taskList = new LinkedList<Task>();

		// 生成播放任务列表，并执行播放
		refleshPlayerTask();
		if (taskList.size() == 1)// TODO:临时性，设置循环播放
		{
			// bPlayLoop = true;
		}
		pollTask();
	}


	@Override
	protected void onDestroy()
	{
		Log.w( TAG, "MediaPlayer is exiting." );
		if (taskList != null)
		{
			taskList.clear();
			taskList = null;
		}
		super.onDestroy();
	}

	/**
	 * 遍历多媒体播放目录，生成任务，并挂载到队列中
	 */
	private void refleshPlayerTask()
	{
		// TODO:未来可以在此函数中增加任务生成策略，从而控制哪些图片先播放，哪些图片播放几次等

		// 刷新得到图片列表
		ArrayList<String> fileList = listofImageFiles(IMAGE_PATH);
		if (fileList != null && fileList.size() > 0)// 含有需要播放的图片
		{
			Task task = new Task(Task.ACTION_PLAY, FILE_TYPE_IMAGE, fileList);
			taskList.add(task);
		}

		// 得到采用硬件或软件加速的视频格式列表
		fileList = listofACLVideoFiles(VIDEO_PATH);
		if (fileList != null && fileList.size() > 0)// 含有需要播放的视频
		{
			Task task = new Task(Task.ACTION_PLAY, FILE_TYPE_VIDEO_ACL,
					fileList);
			taskList.add(task);
		}

		// 刷新得到视频文件列表，把每个视频文件作为一个任务执行
		fileList = listofVideoFiles(VIDEO_PATH);
		if (fileList != null && fileList.size() > 0)
		{
			for (int i = 0; i < fileList.size(); i++)
			{
				ArrayList<String> videoList = new ArrayList<String>();
				videoList.add(fileList.get(i));
				Task task = new Task(Task.ACTION_PLAY, FILE_TYPE_VIDEO,
						videoList);
				taskList.add(task);
			}
		}
	}

	/**
	 * 判断是否是可支持的文件格式
	 * 
	 * @param file
	 *            文件全路径名
	 * @param supportedFileFormats
	 *            支持的文件扩展名列表
	 * @return true表示支持，false表示不支持
	 */
	public Boolean isSupportedFileFormat(String file,
			String[] supportedFileFormats)
	{
		String ext = file.toString();
		String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);// 得到文件扩展名
		if (Arrays.asList(supportedFileFormats).contains(sub_ext.toLowerCase(Locale.ENGLISH)))
		{
			return true; // 若包含在支持的扩展名列表中，则表示支持
		}
		return false;
	}

	/**
	 * 判断是否是可支持的图片文件格式
	 * 
	 * @param file
	 *            文件全路径名
	 * @return true表示支持，false表示不支持
	 */
	public Boolean isImageFile(String file)
	{
		return isSupportedFileFormat(file, supportedImageFormats);
	}

	/**
	 * 判断是否是可支持的视频文件格式
	 * 
	 * @param file
	 *            文件全路径名
	 * @return true表示支持，false表示不支持
	 */
	public Boolean isVideoFile(String file)
	{
		return isSupportedFileFormat(file, supportedVideoFormats);
	}

	/**
	 * 判断是否是可支持的加速的视频文件格式
	 * 
	 * @param file
	 *            文件全路径名
	 * @return true表示支持，false表示不支持
	 */
	public Boolean isACLVideoFile(String file)
	{
		return isSupportedFileFormat(file, supportedAccelerateVideoFormats);
	}

	/**
	 * 获取可播放的资源文件列表
	 * 
	 * @param file
	 *            文件所在的目录
	 * @param fileType
	 *            文件类别
	 * @return 文件列表，每一个项是图片文件的全路径名
	 */
	public ArrayList<String> getFileList(String file, int fileType)
	{
		File inFile = new File(file);
		ArrayList<String> listOfFiles = new ArrayList<String>();

		// 遍历文件夹下的文件
		for (File files : inFile.listFiles())
		{
			String absolutePath = files.getAbsolutePath();// 得到绝对路径
			Log.i(MainActivity.TAG, absolutePath);
			if (files.isFile())
			{
				if ((FILE_TYPE_VIDEO == fileType && isVideoFile(absolutePath))
						|| (FILE_TYPE_IMAGE == fileType && isImageFile(absolutePath))
						|| (FILE_TYPE_VIDEO_ACL == fileType && isACLVideoFile(absolutePath)))
				{
					listOfFiles.add(absolutePath);// 增加到列表中
				}
			}
		}
		return listOfFiles;
	}
	
	//判断文件是否存在
	public static boolean isFileExist(String path) 
	{
		if (path == null) 
		{
			return false;
		}
		try 
		{
			File f = new File(path);
			if (!f.exists()) 
			{
				return false;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 获取可播放的图片列表
	 * 
	 * @param file
	 *            图片文件所在的目录
	 * @return 图片文件列表，每一个项是图片文件的全路径名
	 */
	public ArrayList<String> listofImageFiles(String file)
	{
		if(!isFileExist(file))
		{
			return null;
		}
		return getFileList(file, FILE_TYPE_IMAGE);
	}

	/**
	 * 获取可播放的视频列表
	 * 
	 * @param file
	 *            图片文件所在的目录
	 * @return 视频文件列表，每一个项是视频文件的全路径名
	 */
	public ArrayList<String> listofVideoFiles(String file)
	{
		if(!FileUtils.isFileExit(file))
		{
			return null;
		}
		
		return getFileList(file, FILE_TYPE_VIDEO);
	}

	/**
	 * 获取可加速播放的视频列表，当前只有mp4格式
	 * 
	 * @param file
	 *            图片文件所在的目录
	 * @return 视频文件列表，每一个项是视频文件的全路径名
	 */
	public ArrayList<String> listofACLVideoFiles(String file)
	{
		if(!FileUtils.isFileExit(file))
		{
			return null;
		}
		
		return getFileList(file, FILE_TYPE_VIDEO_ACL);
	}

	// 处理特殊的控制处理事件，比如没有检测到资源文件时，就需要隔一定时间来再次检测播放的资源
	final private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case EVENT_REDETECT:// 重新检测
				refleshPlayerTask();// 重新刷新播放列表
				if (taskList.size() > 0)
				{// 若含有需要播放的资源，就开始播放
					pollTask();
				} else
				{// 继续等待若干秒后检测
					Message redetectMsg = obtainMessage();
					redetectMsg.what = EVENT_REDETECT;
					sendMessageDelayed( redetectMsg, INTERVAL_TIME_REDETECT * 1000);
				}
				break;
			default:
				break;
			}
		}

	};

	/**
	 * 执行任务队列中的第1个任务
	 */
	private void pollTask()
	{
		// 任务队列为空，隔一段时间后，继续检测
		if (taskList.size() == 0)
		{
			Message msg = Message.obtain(handler, EVENT_REDETECT);
			msg.sendToTarget();
			return;
		}
		
		Log.v(MainActivity.TAG, "pollTask");

		// 从队列中取得第一个任务，并执行
		Task task = taskList.pollFirst();
		if (task.getAction() == Task.ACTION_PLAY)
		{
			doPlayTask(task);
		}// TODO: else 其它的动作，作为未来的功能扩展
	}

	/**
	 * 执行播放任务
	 * 
	 * @param task
	 *            待执行的任务
	 */
	private void doPlayTask(Task task)
	{
		Intent intent = null;

		// 根据资源类型来执行播放
		switch (task.getFileType())
		{
		case FILE_TYPE_IMAGE:// 播放图片
			intent = new Intent();
			intent.setClass(this, ImagePlayer.class);
			intent.putExtra("filelist", task.getFileList());
			intent.putExtra("loop", bPlayLoop);// TODO:是否循环播放
			startActivityForResult(intent, REQUEST_CODE_PLAY_IMAGE);
			break;
		case FILE_TYPE_VIDEO:// 播放不可加速的视频
			String pkg = "com.broov.player";
			String className = "com.broov.player.VideoPlayer";
			ComponentName appComponent = new ComponentName(pkg, className);
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.fromFile(new File(task.getFileList().get(0))));
			intent.setComponent(appComponent);
			startActivityForResult(intent, REQUEST_CODE_PLAY_VIDEO);
			break;
		case FILE_TYPE_VIDEO_ACL:// 播放可加速的视频
			intent = new Intent();
			intent.setClass(this, VideoPlayer.class);
			intent.putExtra("filelist", task.getFileList());
			startActivityForResult(intent, REQUEST_CODE_PLAY_VIDEO_ACL);
			break;
		default:
			Log.e(MainActivity.TAG, "Invalid multimedia file type!");
			break;
		}
	}

	/**
	 * 当任务执行完成后，会调用此消息
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.v(MainActivity.TAG, "onActivityResult:play next task");
		
		// 执行下一个任务
		pollTask();
	}
}

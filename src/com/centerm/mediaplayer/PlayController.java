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
 * ������ȡ��ý����Դ�ļ���Ȼ����Ʋ����ĸ�Acitivity��App��ִ�в��š�
 * 
 */
public class PlayController extends CommonActivity
{
	// ��̬��������
	//public static final String IMAGE_PATH = "/vendor/media/picture"; // ͼƬ�ļ����ڵ�Ŀ¼
	//public static final String VIDEO_PATH = "/vendor/media/video"; // ��Ƶ�ļ����ڵ�Ŀ¼
	public static final String IMAGE_PATH = "/mnt/internal_sd/media/picture"; // ͼƬ�ļ����ڵ�Ŀ¼
	public static final String VIDEO_PATH = "/mnt/internal_sd/media/video"; // ��Ƶ�ļ����ڵ�Ŀ¼
	public static final int FILE_TYPE_IMAGE = 0; // ͼƬ�ļ����
	public static final int FILE_TYPE_VIDEO = 1; // ��Ƶ�ļ����
	public static final int FILE_TYPE_VIDEO_ACL = 2; // android��ʵ�ּ��ٵ���Ƶ�ļ����
	public static final int REQUEST_CODE_PLAY_IMAGE = 0; // ����ͼƬ������
	public static final int REQUEST_CODE_PLAY_VIDEO = 1; // ������Ƶ������
	public static final int REQUEST_CODE_PLAY_VIDEO_ACL = 2; // ���ſɼ��ٵ���Ƶ������
	public static final String supportedImageFormats[] = { "gif", "bmp", "png",
			"jpg" };// ֧�ֵ�ͼƬ�ļ���ʽ
	public static final String supportedVideoFormats[] = // ֧�ֵ���Ƶ��ʽ
	{ /* "mp4", */"wmv", "avi", "mkv", "rm", "mpg", "mpeg", "flv", "divx", "swf",
			"dat", "h264", "h263", "h261", "3gp", "3gpp", "asf", "mov", "m4v",
			"ogv", "vob", "vstream", "ts", "webm", "vro", "tts", "tod", "rmvb",
			"rec", "ps", "ogx", "ogm", "nuv", "nsv", "mxf", "mts", "mpv2",
			"mpeg1", "mpeg2", "mpeg4", "mpe", "mp4v", "mp2v", "mp2", "m2ts",
			"m2t", "m2v", "m1v", "amv", "3gp2" };
	public static final String supportedAccelerateVideoFormats[] = { "mp4" };// Ӳ���������ʵ�ּ��ٴ������Ƶ��ʽ
	public static final int EVENT_REDETECT = 1; // ��������ԴΪ�գ���Ҫ���¼��
	public static final int INTERVAL_TIME_REDETECT = 10; // ��������ԴΪ�գ�ÿ10���Ӽ��һ��
	private boolean bPlayLoop = false; // ѭ������
	private static final String TAG = MainActivity.TAG;
	
	// ����
	LinkedList<Task> taskList = null; // �����б�

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(MainActivity.TAG, "playcontroller start");
		super.onCreate(savedInstanceState);
		taskList = new LinkedList<Task>();

		// ���ɲ��������б���ִ�в���
		refleshPlayerTask();
		if (taskList.size() == 1)// TODO:��ʱ�ԣ�����ѭ������
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
	 * ������ý�岥��Ŀ¼���������񣬲����ص�������
	 */
	private void refleshPlayerTask()
	{
		// TODO:δ�������ڴ˺����������������ɲ��ԣ��Ӷ�������ЩͼƬ�Ȳ��ţ���ЩͼƬ���ż��ε�

		// ˢ�µõ�ͼƬ�б�
		ArrayList<String> fileList = listofImageFiles(IMAGE_PATH);
		if (fileList != null && fileList.size() > 0)// ������Ҫ���ŵ�ͼƬ
		{
			Task task = new Task(Task.ACTION_PLAY, FILE_TYPE_IMAGE, fileList);
			taskList.add(task);
		}

		// �õ�����Ӳ����������ٵ���Ƶ��ʽ�б�
		fileList = listofACLVideoFiles(VIDEO_PATH);
		if (fileList != null && fileList.size() > 0)// ������Ҫ���ŵ���Ƶ
		{
			Task task = new Task(Task.ACTION_PLAY, FILE_TYPE_VIDEO_ACL,
					fileList);
			taskList.add(task);
		}

		// ˢ�µõ���Ƶ�ļ��б���ÿ����Ƶ�ļ���Ϊһ������ִ��
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
	 * �ж��Ƿ��ǿ�֧�ֵ��ļ���ʽ
	 * 
	 * @param file
	 *            �ļ�ȫ·����
	 * @param supportedFileFormats
	 *            ֧�ֵ��ļ���չ���б�
	 * @return true��ʾ֧�֣�false��ʾ��֧��
	 */
	public Boolean isSupportedFileFormat(String file,
			String[] supportedFileFormats)
	{
		String ext = file.toString();
		String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);// �õ��ļ���չ��
		if (Arrays.asList(supportedFileFormats).contains(sub_ext.toLowerCase(Locale.ENGLISH)))
		{
			return true; // ��������֧�ֵ���չ���б��У����ʾ֧��
		}
		return false;
	}

	/**
	 * �ж��Ƿ��ǿ�֧�ֵ�ͼƬ�ļ���ʽ
	 * 
	 * @param file
	 *            �ļ�ȫ·����
	 * @return true��ʾ֧�֣�false��ʾ��֧��
	 */
	public Boolean isImageFile(String file)
	{
		return isSupportedFileFormat(file, supportedImageFormats);
	}

	/**
	 * �ж��Ƿ��ǿ�֧�ֵ���Ƶ�ļ���ʽ
	 * 
	 * @param file
	 *            �ļ�ȫ·����
	 * @return true��ʾ֧�֣�false��ʾ��֧��
	 */
	public Boolean isVideoFile(String file)
	{
		return isSupportedFileFormat(file, supportedVideoFormats);
	}

	/**
	 * �ж��Ƿ��ǿ�֧�ֵļ��ٵ���Ƶ�ļ���ʽ
	 * 
	 * @param file
	 *            �ļ�ȫ·����
	 * @return true��ʾ֧�֣�false��ʾ��֧��
	 */
	public Boolean isACLVideoFile(String file)
	{
		return isSupportedFileFormat(file, supportedAccelerateVideoFormats);
	}

	/**
	 * ��ȡ�ɲ��ŵ���Դ�ļ��б�
	 * 
	 * @param file
	 *            �ļ����ڵ�Ŀ¼
	 * @param fileType
	 *            �ļ����
	 * @return �ļ��б�ÿһ������ͼƬ�ļ���ȫ·����
	 */
	public ArrayList<String> getFileList(String file, int fileType)
	{
		File inFile = new File(file);
		ArrayList<String> listOfFiles = new ArrayList<String>();

		// �����ļ����µ��ļ�
		for (File files : inFile.listFiles())
		{
			String absolutePath = files.getAbsolutePath();// �õ�����·��
			Log.i(MainActivity.TAG, absolutePath);
			if (files.isFile())
			{
				if ((FILE_TYPE_VIDEO == fileType && isVideoFile(absolutePath))
						|| (FILE_TYPE_IMAGE == fileType && isImageFile(absolutePath))
						|| (FILE_TYPE_VIDEO_ACL == fileType && isACLVideoFile(absolutePath)))
				{
					listOfFiles.add(absolutePath);// ���ӵ��б���
				}
			}
		}
		return listOfFiles;
	}
	
	//�ж��ļ��Ƿ����
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
	 * ��ȡ�ɲ��ŵ�ͼƬ�б�
	 * 
	 * @param file
	 *            ͼƬ�ļ����ڵ�Ŀ¼
	 * @return ͼƬ�ļ��б�ÿһ������ͼƬ�ļ���ȫ·����
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
	 * ��ȡ�ɲ��ŵ���Ƶ�б�
	 * 
	 * @param file
	 *            ͼƬ�ļ����ڵ�Ŀ¼
	 * @return ��Ƶ�ļ��б�ÿһ��������Ƶ�ļ���ȫ·����
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
	 * ��ȡ�ɼ��ٲ��ŵ���Ƶ�б���ǰֻ��mp4��ʽ
	 * 
	 * @param file
	 *            ͼƬ�ļ����ڵ�Ŀ¼
	 * @return ��Ƶ�ļ��б�ÿһ��������Ƶ�ļ���ȫ·����
	 */
	public ArrayList<String> listofACLVideoFiles(String file)
	{
		if(!FileUtils.isFileExit(file))
		{
			return null;
		}
		
		return getFileList(file, FILE_TYPE_VIDEO_ACL);
	}

	// ��������Ŀ��ƴ����¼�������û�м�⵽��Դ�ļ�ʱ������Ҫ��һ��ʱ�����ٴμ�ⲥ�ŵ���Դ
	final private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case EVENT_REDETECT:// ���¼��
				refleshPlayerTask();// ����ˢ�²����б�
				if (taskList.size() > 0)
				{// ��������Ҫ���ŵ���Դ���Ϳ�ʼ����
					pollTask();
				} else
				{// �����ȴ����������
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
	 * ִ����������еĵ�1������
	 */
	private void pollTask()
	{
		// �������Ϊ�գ���һ��ʱ��󣬼������
		if (taskList.size() == 0)
		{
			Message msg = Message.obtain(handler, EVENT_REDETECT);
			msg.sendToTarget();
			return;
		}
		
		Log.v(MainActivity.TAG, "pollTask");

		// �Ӷ�����ȡ�õ�һ�����񣬲�ִ��
		Task task = taskList.pollFirst();
		if (task.getAction() == Task.ACTION_PLAY)
		{
			doPlayTask(task);
		}// TODO: else �����Ķ�������Ϊδ���Ĺ�����չ
	}

	/**
	 * ִ�в�������
	 * 
	 * @param task
	 *            ��ִ�е�����
	 */
	private void doPlayTask(Task task)
	{
		Intent intent = null;

		// ������Դ������ִ�в���
		switch (task.getFileType())
		{
		case FILE_TYPE_IMAGE:// ����ͼƬ
			intent = new Intent();
			intent.setClass(this, ImagePlayer.class);
			intent.putExtra("filelist", task.getFileList());
			intent.putExtra("loop", bPlayLoop);// TODO:�Ƿ�ѭ������
			startActivityForResult(intent, REQUEST_CODE_PLAY_IMAGE);
			break;
		case FILE_TYPE_VIDEO:// ���Ų��ɼ��ٵ���Ƶ
			String pkg = "com.broov.player";
			String className = "com.broov.player.VideoPlayer";
			ComponentName appComponent = new ComponentName(pkg, className);
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.fromFile(new File(task.getFileList().get(0))));
			intent.setComponent(appComponent);
			startActivityForResult(intent, REQUEST_CODE_PLAY_VIDEO);
			break;
		case FILE_TYPE_VIDEO_ACL:// ���ſɼ��ٵ���Ƶ
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
	 * ������ִ����ɺ󣬻���ô���Ϣ
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.v(MainActivity.TAG, "onActivityResult:play next task");
		
		// ִ����һ������
		pollTask();
	}
}

package com.centerm.mediaplayer.second;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;
import android.widget.Toast;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.centerm.mediaplayer.CommonActivity;
import com.centerm.mediaplayer.MainActivity;

/**
 * 根据boc_ad.xml来解析播放列表，然后控制采用哪个Acitivity或App来执行播放。
 * 
 */
public class NewPlayController extends CommonActivity
{
	// 静态变量定义
	//public static final String IMAGE_PATH = "/vendor/media/picture"; // 图片文件所在的目录
	//public static final String VIDEO_PATH = "/vendor/media/video"; // 视频文件所在的目录
	public static final String IMAGE_PATH = "/mnt/internal_sd/media/picture/"; // 图片文件所在的目录
	public static final String VIDEO_PATH = "/mnt/internal_sd/media/video/"; // 视频文件所在的目录
	public static final int FILE_TYPE_IMAGE = 0; // 图片文件类别
	public static final int FILE_TYPE_VIDEO = 1; // 视频文件类别
	public static final int FILE_TYPE_VIDEO_ACL = 2; // android已实现加速的视频文件类别
	public static final int REQUEST_CODE_PLAY_IMAGE = 0; // 播放图片的请求
	public static final int REQUEST_CODE_PLAY_VIDEO = 1; // 播放视频的请求
	public static final int REQUEST_CODE_PLAY_VIDEO_ACL = 2; // 播放可加速的视频的请求
	public static final String supportedImageFormats[] = { "gif", "bmp", "png", "jpg" };// 支持的图片文件格式
	public static final String supportedVideoFormats[] = // 支持的视频格式
		{ /* "mp4", */"h264", "h263", "h261", "vstream", "ts", "webm", "vro", "tts", "tod",
		"rec", "ps", "ogx", "ogm", "nuv", "nsv", "mxf", "mts", "mpv2",
		"mpeg1", "mpeg2", "mp2v", "mp2", "m2ts", "m2t", "m2v", "m1v", "3gp2" };
	public static final String supportedAccelerateVideoFormats[] = { "mp4", "flv", "avi", "wmv", "mkv", "rm",
		"mpg", "mpeg", "divx", "swf", "dat", "3gp", "3gpp", "asf", "mov", "m4v",
		"ogv", "vob", "rmvb", "mpeg4", "mpe", "mp4v", "amv" };// 硬件或软件已实现加速处理的视频格式
	public static final int EVENT_REDETECT = 1; // 若播放资源为空，需要重新检测
	public static final int INTERVAL_TIME_REDETECT = 10; // 若播放资源为空，每10秒钟检测一次
	private boolean bPlayLoop = false; // 循环播放
	private static final String TAG = MainActivity.TAG;

	private static final String XMLPATH = "/mnt/internal_sd/config/boc_ad.xml";
	private static final String MODE = "1";
	private static final String INTERVAL = "5";

	// 变量
	LinkedList<NewTask> taskList = null; // 任务列表

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(MainActivity.TAG, "playcontroller start");
		super.onCreate(savedInstanceState);
		taskList = new LinkedList<NewTask>();

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

	private boolean makeAdList(){
		ArrayList<String> imgList = listofImageFiles(IMAGE_PATH);
		ArrayList<String> videoList = listofACLVideoFiles(VIDEO_PATH);
		ArrayList<String> totalList = new ArrayList<String>();
		for(String imgName : imgList){ 
			totalList.add(imgName);
		}
		for(String videoName : videoList){
			totalList.add(videoName);
		}

		//创建根节点
		try {  
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
			DocumentBuilder builder = factory.newDocumentBuilder();  
			Document document = builder.newDocument();  

			Element root = document.createElement("list");   //创建根节点  
			document.appendChild(root);  

			for(String value : totalList){
				Element item = document.createElement("file");  
				Attr name = document.createAttribute("name");  
				name.setValue(value);  
				item.setAttributeNode(name);  
				Attr mode = document.createAttribute("mode");  
				mode.setValue(MODE);  
				item.setAttributeNode(mode); 
				Attr interval = document.createAttribute("Interval");  
				interval.setValue(INTERVAL);  
				item.setAttributeNode(interval); 
				root.appendChild(item);  
			}

			//将DOM对象document写入到xml文件中  
			TransformerFactory tf = TransformerFactory.newInstance();  
			try {  
				Transformer transformer = tf.newTransformer();  
				DOMSource source = new DOMSource(document);  
				transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");  
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");  
				PrintWriter pw = new PrintWriter(new FileOutputStream(XMLPATH));  
				StreamResult result = new StreamResult(pw);  
				transformer.transform(source, result);     //关键转换  
				System.out.println("生成XML文件成功!");  
			} catch (TransformerConfigurationException e) {  
				System.out.println(e.getMessage());  
			} catch (IllegalArgumentException e) {  
				System.out.println(e.getMessage());  
			} catch (FileNotFoundException e) {  
				System.out.println(e.getMessage());  
			} catch (TransformerException e) {  
				System.out.println(e.getMessage());  
			}  

			return true;
		} catch (ParserConfigurationException e) {  
			e.printStackTrace();
		}  

		return false;
	}

	/**
	 * 遍历多媒体播放目录，生成任务，并挂载到队列中
	 */
	private void refleshPlayerTask()
	{

		File file = new File(XMLPATH);
		if(!file.exists()){
			//生成播放列表
			boolean is = makeAdList();
			if(!is){
				Log.e(TAG, "播放列表生成失败!");
				return;
			}
		}
		
		//解析boc_ad.xml 
		List<NewFile> list = getNewFileList(file);
		if(list == null){
			return;
		}
		for(NewFile newFile : list){
			String name = newFile.getName();
			NewTask task = null;
			if(isImageFile(name)){ //是图片
				newFile.setName(IMAGE_PATH+name);
				task = new NewTask(NewTask.ACTION_PLAY, FILE_TYPE_IMAGE, newFile);
			}else if(isVideoFile(name)){
				newFile.setName(VIDEO_PATH+name);
				task = new NewTask(NewTask.ACTION_PLAY, FILE_TYPE_VIDEO, newFile);
			}else if(isACLVideoFile(name)){
				newFile.setName(VIDEO_PATH+name);
				task = new NewTask(NewTask.ACTION_PLAY, FILE_TYPE_VIDEO_ACL, newFile);
			}
			taskList.add(task);
		}
	}

	private List<NewFile> getNewFileList(File mFile) {
		try{
			List<NewFile> tFileList = new ArrayList<NewFile>();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new FileInputStream(mFile));
			Element root = document.getDocumentElement(); //获取根节点
			NodeList nodes = root.getElementsByTagName("file");
			for(int i=0; i<nodes.getLength(); i++){
				Element tFileElement = (Element)nodes.item(i);
				NewFile newFile = new NewFile();
				newFile.setName(tFileElement.getAttribute("name"));
				newFile.setMode(Integer.parseInt(tFileElement.getAttribute("mode")));
				newFile.setInterval(Integer.parseInt(tFileElement.getAttribute("Interval")));
				tFileList.add(newFile);
			}
			return tFileList;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
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
			String fileName = files.getName();// 得到绝对路径
			Log.i(MainActivity.TAG, fileName);
			if (files.isFile())
			{
				if ((FILE_TYPE_VIDEO == fileType && isVideoFile(fileName))
						|| (FILE_TYPE_IMAGE == fileType && isImageFile(fileName))
						|| (FILE_TYPE_VIDEO_ACL == fileType && isACLVideoFile(fileName)))
				{
					listOfFiles.add(fileName);// 增加到列表中
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
		if(!isFileExist(file))
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
		if(!isFileExist(file))
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
		NewTask task = taskList.pollFirst();
		if(task == null){
			pollTask();
			return;
		}
		//判断文件是否存在
		if(!isFileExist(task.getNewFile().getName())){
			pollTask();
			return;
		}

		if (task.getAction() == NewTask.ACTION_PLAY)
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
	private void doPlayTask(NewTask task)
	{
		Intent intent = null;

		// 根据资源类型来执行播放
		switch (task.getFileType())
		{
		case FILE_TYPE_IMAGE:// 播放图片
			intent = new Intent();
			intent.setClass(this, NewImagePlayer.class);
			intent.putExtra("newfile", task.getNewFile());
			intent.putExtra("loop", bPlayLoop);// TODO:是否循环播放
			startActivityForResult(intent, REQUEST_CODE_PLAY_IMAGE);
			break;
		case FILE_TYPE_VIDEO:// 播放不可加速的视频
			try{
				String pkg = "com.broov.player";
				String className = "com.broov.player.VideoPlayer";
				ComponentName appComponent = new ComponentName(pkg, className);
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.fromFile(new File(task.getNewFile().getName())));
				intent.setComponent(appComponent);
				startActivityForResult(intent, REQUEST_CODE_PLAY_VIDEO);
			}catch(Exception e){
				e.printStackTrace();	
			}
			break;
		case FILE_TYPE_VIDEO_ACL:// 播放可加速的视频
			intent = new Intent();
			intent.setClass(this, NewVideoPlayer.class);
			intent.putExtra("newfile", task.getNewFile());
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

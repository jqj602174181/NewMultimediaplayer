package com.centerm.mediaplayer.second;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
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

import pl.droidsonroids.gif.GifImageView;

import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.ViewFlipper;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.centerm.commons.utils.FileUtils;
import com.centerm.commons.utils.StringUtils;
import com.centerm.mediaplayer.CommonActivity;
import com.centerm.mediaplayer.MainActivity;
import com.centerm.mediaplayer.R;
import com.centerm.mediaplayer.image.ShutterImageView;

/**
 * 根据boc_ad.xml来解析播放列表，然后控制采用哪个Acitivity或App来执行播放。
 * 
 */
public class NewPlayControllerEX extends CommonActivity
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

	private ViewFlipper mViewFlipper = null; // 播放图片的容器
	private VideoView videoView = null; // 视频视图
	private MediaController mediaController = null;

	private String mFilePath = null;	//当前正在播放状态的图片路径名
	private int animationType = 0; // 图片动画效果类型
	private final static int XML_ANIM_TYPE_COUNT = 5;// 采用XML实现的动画效果的个数
	private final static int ANIM_TYPE_COUNT = XML_ANIM_TYPE_COUNT + 2; // 动画效果的个数（加上百叶窗的2种效果）
	private static SparseArray<Animation> animations = null;// 动画的效果,
	private int mScreenWidth = 1024; // 屏幕宽度
	private int mScreenHeight = 600; // 屏幕高度
	private ShutterImageView shutterImageView = null;// 百叶窗视图
	private long originalTime = 0, currentTime = 0;
	private final static int DEF_INTERVAL_TIME = 5; // 默认图片切换时间为5秒一张
	private int mIntervalTimes = DEF_INTERVAL_TIME * 1000; // 图片切换的间隔时间，TODO:未来应该根据配置来更新该值
	private final static int MSG_PLAY_NEXT = 990; // 播放下一张图片
	private boolean isPause = false; // 是否处理于暂停状态

	private boolean isPlaying = false; // 是否正在播放
	private int playPosition = 0; // 在暂停时，用于记录暂停的位置，以便恢复播放
	private GifImageView gifView = null;
	private boolean isImageFirst = true;
	private boolean isVideoFirst = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(MainActivity.TAG, "playcontroller start");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageandvideo);

		taskList = new LinkedList<NewTask>();

		mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		// 准备开始播放视频
		videoView = (VideoView) findViewById(R.id.videoView);
		mediaController = new MediaController(this);
		videoView.setMediaController(mediaController);
		videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				playNext();
			}
		});

		// 注册视频播放执行错误的监听器
		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
		{
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra)
			{
				playNext();// 播放下一个, TODO:发生错误时，应该告诉Controller，尝试采用未加速的播放器进行播放
				return true;// true表示直接播放下一个
			}
		});
		videoView.setFocusable(false);
		videoView.setFocusableInTouchMode(false);
		videoView.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				//收到组合键处理，则启动设置界面
				Log.i("video", String.format("onkeydown,keycode=%d,%d",arg1, arg2.getKeyCode()));
				if (arg2.getAction() == KeyEvent.ACTION_DOWN && KEY_COMB == arg1)
				{
					playKeySound();

					// 发送广播
					final String ACTION = "com.centerm.media.START_APP_ACTION";
					Intent intent = new Intent(); // 实例化Intent
					intent.setAction(ACTION);
					sendBroadcast(intent);
					Log.i(MainActivity.TAG, "send broadcast");
				}
				return true;
			}
		});

		getDisplayInfo();
		loadAnimations();//加载动作资源		

		// 生成播放任务列表，并执行播放
		refleshPlayerTask();
		if (taskList.size() == 1)// TODO:临时性，设置循环播放
		{
			// bPlayLoop = true;
		}
		pollTask();
	}

	public void playNext()
	{
		pollTask();
	}


	@Override
	protected void onDestroy()
	{
		if (taskList != null)
		{
			taskList.clear();
			taskList = null;
		}
		cleanup();
		super.onDestroy();
	}

	private void cleanup()
	{
		// 清除图片缓存
		if (mViewFlipper != null)
		{
			// 清除最后一张图片的资源
			ImageView imageView = (ImageView) mViewFlipper.getChildAt(0);
			if (imageView != null)
			{
				cleanImageView(imageView);
				mViewFlipper.removeViewAt(0);
			}
		}
	}

	private void cleanImageView(ImageView view)
	{
		// 回收图片资源
		/*BitmapDrawable bitmapDrawable = (BitmapDrawable) view.getBackground();
		if (bitmapDrawable != null)
		{
			Bitmap bitmap = bitmapDrawable.getBitmap();
			if (!bitmap.isRecycled())
			{
				bitmap.recycle();
				bitmap = null;
			}
		}*/
		view.setImageResource(0);
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
		}
	}

	/**
	 * 执行播放任务
	 * 
	 * @param task
	 *            待执行的任务
	 */
	private void doPlayTask(NewTask task)
	{
		NewFile mNewFile = task.getNewFile();
		mFilePath = mNewFile.getName();
		Log.e(TAG, "mFilePath:" + mFilePath);
		Intent intent = null;
		// 根据资源类型来执行播放
		switch (task.getFileType())
		{
		case FILE_TYPE_IMAGE:// 播放图片
			mViewFlipper.setVisibility(View.VISIBLE);
			videoView.setVisibility(View.GONE);
			mIntervalTimes = mNewFile.getInterval() * 1000;
			if(isImageFirst){
				initImages();//初始化广告图片控件，并加载第一张图片
			}else{
				playNextImage();
			}
			isImageFirst = false;
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
			videoView.setVisibility(View.VISIBLE);
			mViewFlipper.setVisibility(View.GONE);
			if(isVideoFirst){
				videoView.setVideoPath(mFilePath);
			}else{
				videoView.stopPlayback();// 结束上一次的播放
				videoView.setVideoPath(mFilePath);
				videoView.start();
			}
			isVideoFirst = false;
			break;
		default:
			Log.e(MainActivity.TAG, "Invalid multimedia file type!");
			break;
		}
	}

	private void playNextImage() {

		resetFileAndAnimation(0); // 重新分配图片文件和动画类型

		if(!FileUtils.isFileExit(mFilePath))
		{
			mHandler.removeMessages(MSG_PLAY_NEXT);
			return;
		}

		// 加载下一张图片的view，并显示
		ImageView imageView = createImageView(mFilePath);
		mViewFlipper.addView(imageView);
		mViewFlipper.showNext();// 显示下一张图片

		// 清除前一张图片的资源
		imageView = (ImageView) mViewFlipper.getChildAt(0);
		cleanImageView(imageView);
		mViewFlipper.removeViewAt(0);

		// 获取当前时间
		originalTime = System.currentTimeMillis();
	}

	private void initImages()
	{
		resetFileAndAnimation(0);//选择文件和动画类型
		ImageView imageView = createImageView(mFilePath);
		mViewFlipper.addView(imageView);

		//记录最后更新图片显示的时间
		originalTime = System.currentTimeMillis();

		// 如果第一张图片是gif就不用发消息，因为gif会通过AnimationEnd通知
		if (suffixisGIF(mFilePath))
		{
			return;
		}

		handleState();//间隔一定时间后，检测播放状态
	}

	private void handleState()
	{
		Message msg = mHandler.obtainMessage(MSG_PLAY_NEXT);
		mHandler.sendMessageDelayed(msg, mIntervalTimes);
	}

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_PLAY_NEXT://播放下一张图片
				if (judgeValidMsg())//判断当前消息是否应该处理
				{
					doPlayNext();
				}
				break;

			default:
				break;
			}
		}

		/**
		 * 判断“播放下一张图片”的消息是否应该处理
		 * @return true表示应该处理，false表示不应该处理
		 */
		private boolean judgeValidMsg()
		{
			// 比较2次时间是否超过间隔，若没有，就丢弃。
			currentTime = System.currentTimeMillis();
			if ((currentTime - originalTime) < mIntervalTimes-2)
			{
				return false;
			}

			if (isPause)// 如果处于暂停状态，就不进行切换了
			{
				return false;
			}

			return true;
		}

		private void doPlayNext()
		{
			// 如果是最后的一张图片，且没有循环播放的要求，就结束当前activity
			if (true)
			{
				mHandler.removeMessages(MSG_PLAY_NEXT);
				pollTask();
				return;
			}
		}
	};

	private ImageView createImageView(String filepath)
	{
		ImageView image = null;

		if (suffixisGIF(filepath))//gif图片
		{
			// gif图片，不采用任何动画，因为它自身有动画
			mViewFlipper.clearAnimation();
			mViewFlipper.setInAnimation(animations.get(0));
			mViewFlipper.setOutAnimation(animations.get(1));

			gifView = new GifImageView( NewPlayControllerEX.this );
			gifView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			gifView.setScaleType(ImageView.ScaleType.FIT_XY);
			gifView.setImageURI( Uri.fromFile( new File(filepath)));
			image = (ImageView) gifView;
			//			gifView = new GifImageView(this, mFilePath);
			//
			//			//			Log.v(TAG, "gif设置Listener");
			//			gifView.setAnimationListener(mListener);
			//			image = (ImageView) gifView;
		} else
		{
			// 除gif图片外的所有图片处理，需要采用随机动画
			image = createGeneralImageView(filepath);
		}

		return image;
	}

	private ImageView createGeneralImageView(String filepath)
	{
		ImageView image = null;

		if (animationType >= XML_ANIM_TYPE_COUNT)// 百叶窗，采用特殊的view
		{
			shutterImageView = new ShutterImageView(this);
			if ((animationType % 2) == 0)// 随机选择水平或垂直
			{
				shutterImageView.setOrientation(ShutterImageView.HORIZENTAL);
			}

			shutterImageView.setAnimationListener(mListener);
			image = (ShutterImageView) shutterImageView;
		} else
		{
			image = new ImageView(this);
		}

		image.setImageBitmap(decodeImage(filepath)); // 设置Bitmap
		image.setScaleType(ScaleType.CENTER);// 居中显示，但不放大，避免小图像被放大
		return image;
	}

	private Bitmap decodeImage(String filePath)
	{
		// 先得到图像的大小
		int inSampleSize = 1; // 采样大小
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);// 只是解码得到大小
		int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;

		// 先进行下采样，得到最小适合屏幕大小的图像
		if (imageWidth > mScreenWidth || imageHeight > mScreenHeight)
		{
			int widthSampleSize = (int) ((float) (imageWidth) / (float) mScreenWidth);
			int heightSampleSize = (int) ((float) (imageHeight) / (float) mScreenHeight);
			inSampleSize = widthSampleSize > heightSampleSize ? widthSampleSize
					: heightSampleSize;
		}
		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		options.inJustDecodeBounds = false;// 标记此次解码不是得到图片大小,而是得到数据
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

		// 若解码出错，显示error图片
		if (bitmap == null)
		{
			return BitmapFactory.decodeResource(getResources(),
					R.drawable.error);
		}

		// 判断是否仍然比屏幕大小更大，就需要采用精确缩小方式
		imageWidth = bitmap.getWidth();
		imageHeight = bitmap.getHeight();
		if (imageWidth > mScreenWidth || imageHeight > mScreenHeight)
		{
			// 计算缩小的比例值
			float xScale = (float) imageWidth / (float) mScreenWidth;
			float yScale = (float) imageHeight / (float) mScreenHeight;
			float scale = xScale > yScale ? (float) 1.0 / xScale : (float) 1.0
					/ yScale;

			// 准备缩放
			Matrix matrix = new Matrix();
			matrix.setScale(scale, scale);

			Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
					imageWidth, imageHeight, matrix, false);
			return resizedBitmap;
		}// else 返回未缩放前的图片
		return bitmap;
	}

	private void resetFileAndAnimation(int index)
	{
		// 获取待播放文件
		if (suffixisGIF(mFilePath))
		{
			animationType = 0;
			return;
		}

		// 得到一个随机数，作为动画效果类型
		animationType = (int) (100 * Math.random()) % ANIM_TYPE_COUNT;
		mViewFlipper.clearAnimation();

		// 百叶窗不需要设置动画
		if (animationType >= XML_ANIM_TYPE_COUNT)
		{
			return;
		}

		// 采用标准的动画方法实现
		int key = 2 * animationType;
		mViewFlipper.setInAnimation(animations.get(key));
		mViewFlipper.setOutAnimation(animations.get(key + 1));
		animations.get(key).setAnimationListener(mListener);
	}

	private boolean suffixisGIF(String filename)
	{
		// 获取文件后缀，如果与文件名相同，说明不存在文件扩展名
		String suffix = StringUtils.substringAfterLast(filename, ".");
		if (StringUtils.equals(filename, suffix))
		{
			return false;
		}
		// 将文件扩展名转成大写，并判断是否为GIF
		suffix = StringUtils.upperCase(suffix, Locale.ENGLISH);
		return StringUtils.equals(suffix, "GIF");
	}

	private void getDisplayInfo()
	{
		// 得到屏幕设备信息
		Point displaySize = new Point();
		Display display = getWindowManager().getDefaultDisplay();

		// 采用反射机制，执行隐藏的API接口
		try
		{
			Class<?> displayCls = Class.forName("android.view.Display");
			Method methodGetRealSize = displayCls.getMethod("getRealSize",
					Point.class);
			methodGetRealSize.invoke(display, displaySize);
		} catch (Exception e)
		{
			e.printStackTrace();
			display.getSize(displaySize);// 如果发生异常，就得到去除Status后的大小
		}

		mScreenWidth = displaySize.x;
		mScreenHeight = displaySize.y;
	}

	private void loadAnimations()
	{
		// 加载动画
		if (animations == null)
		{
			animations = new SparseArray<Animation>();
			for (int i = 0; i < XML_ANIM_TYPE_COUNT * 2; i++)
			{
				Animation animation = AnimationUtils.loadAnimation(this,
						R.anim.push_left_in + i);
				animations.put(i, animation);
			}
		}		
	}

	final AnimationListener mListener = new AnimationListener()
	{
		@Override
		public void onAnimationEnd(Animation animation)
		{
			shutterImageView = null;// 置为空，释放原始的百叶窗view
			if(animationType < XML_ANIM_TYPE_COUNT && animation != null)
			{
				mViewFlipper.clearAnimation();
				animations.get(animationType*2).setAnimationListener(null);
			}
			handleState();
		}

		@Override
		public void onAnimationRepeat(Animation animation)
		{
		}

		@Override
		public void onAnimationStart(Animation animation)
		{
		}
	};

	@Override
	public void onResume()
	{
		if (isPause)//如果是暂停状态，需要启动以继续播放
		{
			if (!(suffixisGIF(mFilePath)))
			{
				handleState();
			}
		}

		if (isPlaying == false)// 还从未播放，就开始播放
		{
			videoView.start();
			isPlaying = true;
		} else
			// 恢复播放
		{
			videoView.seekTo(playPosition);
			videoView.start();
		}

		isPause = false;// 继续播放
		super.onResume();
	}

	@Override
	public void onPause()
	{
		// 执行暂停播放的功能
		isPause = true;

		if (videoView.isPlaying())// 如果正在播放视频，就暂停播放
		{
			playPosition = videoView.getCurrentPosition();
			videoView.pause();
		}

		super.onPause();
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

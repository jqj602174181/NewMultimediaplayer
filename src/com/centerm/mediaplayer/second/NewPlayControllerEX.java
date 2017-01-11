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
 * ����boc_ad.xml�����������б�Ȼ����Ʋ����ĸ�Acitivity��App��ִ�в��š�
 * 
 */
public class NewPlayControllerEX extends CommonActivity
{
	// ��̬��������
	//public static final String IMAGE_PATH = "/vendor/media/picture"; // ͼƬ�ļ����ڵ�Ŀ¼
	//public static final String VIDEO_PATH = "/vendor/media/video"; // ��Ƶ�ļ����ڵ�Ŀ¼
	public static final String IMAGE_PATH = "/mnt/internal_sd/media/picture/"; // ͼƬ�ļ����ڵ�Ŀ¼
	public static final String VIDEO_PATH = "/mnt/internal_sd/media/video/"; // ��Ƶ�ļ����ڵ�Ŀ¼
	public static final int FILE_TYPE_IMAGE = 0; // ͼƬ�ļ����
	public static final int FILE_TYPE_VIDEO = 1; // ��Ƶ�ļ����
	public static final int FILE_TYPE_VIDEO_ACL = 2; // android��ʵ�ּ��ٵ���Ƶ�ļ����
	public static final int REQUEST_CODE_PLAY_IMAGE = 0; // ����ͼƬ������
	public static final int REQUEST_CODE_PLAY_VIDEO = 1; // ������Ƶ������
	public static final int REQUEST_CODE_PLAY_VIDEO_ACL = 2; // ���ſɼ��ٵ���Ƶ������
	public static final String supportedImageFormats[] = { "gif", "bmp", "png", "jpg" };// ֧�ֵ�ͼƬ�ļ���ʽ
	public static final String supportedVideoFormats[] = // ֧�ֵ���Ƶ��ʽ
		{ /* "mp4", */"h264", "h263", "h261", "vstream", "ts", "webm", "vro", "tts", "tod",
		"rec", "ps", "ogx", "ogm", "nuv", "nsv", "mxf", "mts", "mpv2",
		"mpeg1", "mpeg2", "mp2v", "mp2", "m2ts", "m2t", "m2v", "m1v", "3gp2" };
	public static final String supportedAccelerateVideoFormats[] = { "mp4", "flv", "avi", "wmv", "mkv", "rm",
		"mpg", "mpeg", "divx", "swf", "dat", "3gp", "3gpp", "asf", "mov", "m4v",
		"ogv", "vob", "rmvb", "mpeg4", "mpe", "mp4v", "amv" };// Ӳ���������ʵ�ּ��ٴ������Ƶ��ʽ
	public static final int EVENT_REDETECT = 1; // ��������ԴΪ�գ���Ҫ���¼��
	public static final int INTERVAL_TIME_REDETECT = 10; // ��������ԴΪ�գ�ÿ10���Ӽ��һ��
	private boolean bPlayLoop = false; // ѭ������
	private static final String TAG = MainActivity.TAG;

	private static final String XMLPATH = "/mnt/internal_sd/config/boc_ad.xml";
	private static final String MODE = "1";
	private static final String INTERVAL = "5";

	// ����
	LinkedList<NewTask> taskList = null; // �����б�

	private ViewFlipper mViewFlipper = null; // ����ͼƬ������
	private VideoView videoView = null; // ��Ƶ��ͼ
	private MediaController mediaController = null;

	private String mFilePath = null;	//��ǰ���ڲ���״̬��ͼƬ·����
	private int animationType = 0; // ͼƬ����Ч������
	private final static int XML_ANIM_TYPE_COUNT = 5;// ����XMLʵ�ֵĶ���Ч���ĸ���
	private final static int ANIM_TYPE_COUNT = XML_ANIM_TYPE_COUNT + 2; // ����Ч���ĸ��������ϰ�Ҷ����2��Ч����
	private static SparseArray<Animation> animations = null;// ������Ч��,
	private int mScreenWidth = 1024; // ��Ļ���
	private int mScreenHeight = 600; // ��Ļ�߶�
	private ShutterImageView shutterImageView = null;// ��Ҷ����ͼ
	private long originalTime = 0, currentTime = 0;
	private final static int DEF_INTERVAL_TIME = 5; // Ĭ��ͼƬ�л�ʱ��Ϊ5��һ��
	private int mIntervalTimes = DEF_INTERVAL_TIME * 1000; // ͼƬ�л��ļ��ʱ�䣬TODO:δ��Ӧ�ø������������¸�ֵ
	private final static int MSG_PLAY_NEXT = 990; // ������һ��ͼƬ
	private boolean isPause = false; // �Ƿ�������ͣ״̬

	private boolean isPlaying = false; // �Ƿ����ڲ���
	private int playPosition = 0; // ����ͣʱ�����ڼ�¼��ͣ��λ�ã��Ա�ָ�����
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
		// ׼����ʼ������Ƶ
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

		// ע����Ƶ����ִ�д���ļ�����
		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
		{
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra)
			{
				playNext();// ������һ��, TODO:��������ʱ��Ӧ�ø���Controller�����Բ���δ���ٵĲ��������в���
				return true;// true��ʾֱ�Ӳ�����һ��
			}
		});
		videoView.setFocusable(false);
		videoView.setFocusableInTouchMode(false);
		videoView.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				//�յ���ϼ��������������ý���
				Log.i("video", String.format("onkeydown,keycode=%d,%d",arg1, arg2.getKeyCode()));
				if (arg2.getAction() == KeyEvent.ACTION_DOWN && KEY_COMB == arg1)
				{
					playKeySound();

					// ���͹㲥
					final String ACTION = "com.centerm.media.START_APP_ACTION";
					Intent intent = new Intent(); // ʵ����Intent
					intent.setAction(ACTION);
					sendBroadcast(intent);
					Log.i(MainActivity.TAG, "send broadcast");
				}
				return true;
			}
		});

		getDisplayInfo();
		loadAnimations();//���ض�����Դ		

		// ���ɲ��������б���ִ�в���
		refleshPlayerTask();
		if (taskList.size() == 1)// TODO:��ʱ�ԣ�����ѭ������
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
		// ���ͼƬ����
		if (mViewFlipper != null)
		{
			// ������һ��ͼƬ����Դ
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
		// ����ͼƬ��Դ
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

		//�������ڵ�
		try {  
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
			DocumentBuilder builder = factory.newDocumentBuilder();  
			Document document = builder.newDocument();  

			Element root = document.createElement("list");   //�������ڵ�  
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

			//��DOM����documentд�뵽xml�ļ���  
			TransformerFactory tf = TransformerFactory.newInstance();  
			try {  
				Transformer transformer = tf.newTransformer();  
				DOMSource source = new DOMSource(document);  
				transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");  
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");  
				PrintWriter pw = new PrintWriter(new FileOutputStream(XMLPATH));  
				StreamResult result = new StreamResult(pw);  
				transformer.transform(source, result);     //�ؼ�ת��  
				System.out.println("����XML�ļ��ɹ�!");  
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
	 * ������ý�岥��Ŀ¼���������񣬲����ص�������
	 */
	private void refleshPlayerTask()
	{

		File file = new File(XMLPATH);
		if(!file.exists()){
			//���ɲ����б�
			boolean is = makeAdList();
			if(!is){
				Log.e(TAG, "�����б�����ʧ��!");
				return;
			}
		}

		//����boc_ad.xml 
		List<NewFile> list = getNewFileList(file);
		if(list == null){
			return;
		}
		for(NewFile newFile : list){
			String name = newFile.getName();
			NewTask task = null;
			if(isImageFile(name)){ //��ͼƬ
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
			Element root = document.getDocumentElement(); //��ȡ���ڵ�
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
			String fileName = files.getName();// �õ�����·��
			Log.i(MainActivity.TAG, fileName);
			if (files.isFile())
			{
				if ((FILE_TYPE_VIDEO == fileType && isVideoFile(fileName))
						|| (FILE_TYPE_IMAGE == fileType && isImageFile(fileName))
						|| (FILE_TYPE_VIDEO_ACL == fileType && isACLVideoFile(fileName)))
				{
					listOfFiles.add(fileName);// ���ӵ��б���
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
		if(!isFileExist(file))
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
		if(!isFileExist(file))
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
		NewTask task = taskList.pollFirst();
		if(task == null){
			pollTask();
			return;
		}
		//�ж��ļ��Ƿ����
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
	 * ִ�в�������
	 * 
	 * @param task
	 *            ��ִ�е�����
	 */
	private void doPlayTask(NewTask task)
	{
		NewFile mNewFile = task.getNewFile();
		mFilePath = mNewFile.getName();
		Log.e(TAG, "mFilePath:" + mFilePath);
		Intent intent = null;
		// ������Դ������ִ�в���
		switch (task.getFileType())
		{
		case FILE_TYPE_IMAGE:// ����ͼƬ
			mViewFlipper.setVisibility(View.VISIBLE);
			videoView.setVisibility(View.GONE);
			mIntervalTimes = mNewFile.getInterval() * 1000;
			if(isImageFirst){
				initImages();//��ʼ�����ͼƬ�ؼ��������ص�һ��ͼƬ
			}else{
				playNextImage();
			}
			isImageFirst = false;
			break;
		case FILE_TYPE_VIDEO:// ���Ų��ɼ��ٵ���Ƶ
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
		case FILE_TYPE_VIDEO_ACL:// ���ſɼ��ٵ���Ƶ
			videoView.setVisibility(View.VISIBLE);
			mViewFlipper.setVisibility(View.GONE);
			if(isVideoFirst){
				videoView.setVideoPath(mFilePath);
			}else{
				videoView.stopPlayback();// ������һ�εĲ���
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

		resetFileAndAnimation(0); // ���·���ͼƬ�ļ��Ͷ�������

		if(!FileUtils.isFileExit(mFilePath))
		{
			mHandler.removeMessages(MSG_PLAY_NEXT);
			return;
		}

		// ������һ��ͼƬ��view������ʾ
		ImageView imageView = createImageView(mFilePath);
		mViewFlipper.addView(imageView);
		mViewFlipper.showNext();// ��ʾ��һ��ͼƬ

		// ���ǰһ��ͼƬ����Դ
		imageView = (ImageView) mViewFlipper.getChildAt(0);
		cleanImageView(imageView);
		mViewFlipper.removeViewAt(0);

		// ��ȡ��ǰʱ��
		originalTime = System.currentTimeMillis();
	}

	private void initImages()
	{
		resetFileAndAnimation(0);//ѡ���ļ��Ͷ�������
		ImageView imageView = createImageView(mFilePath);
		mViewFlipper.addView(imageView);

		//��¼������ͼƬ��ʾ��ʱ��
		originalTime = System.currentTimeMillis();

		// �����һ��ͼƬ��gif�Ͳ��÷���Ϣ����Ϊgif��ͨ��AnimationEnd֪ͨ
		if (suffixisGIF(mFilePath))
		{
			return;
		}

		handleState();//���һ��ʱ��󣬼�ⲥ��״̬
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
			case MSG_PLAY_NEXT://������һ��ͼƬ
				if (judgeValidMsg())//�жϵ�ǰ��Ϣ�Ƿ�Ӧ�ô���
				{
					doPlayNext();
				}
				break;

			default:
				break;
			}
		}

		/**
		 * �жϡ�������һ��ͼƬ������Ϣ�Ƿ�Ӧ�ô���
		 * @return true��ʾӦ�ô���false��ʾ��Ӧ�ô���
		 */
		private boolean judgeValidMsg()
		{
			// �Ƚ�2��ʱ���Ƿ񳬹��������û�У��Ͷ�����
			currentTime = System.currentTimeMillis();
			if ((currentTime - originalTime) < mIntervalTimes-2)
			{
				return false;
			}

			if (isPause)// ���������ͣ״̬���Ͳ������л���
			{
				return false;
			}

			return true;
		}

		private void doPlayNext()
		{
			// ���������һ��ͼƬ����û��ѭ�����ŵ�Ҫ�󣬾ͽ�����ǰactivity
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

		if (suffixisGIF(filepath))//gifͼƬ
		{
			// gifͼƬ���������κζ�������Ϊ�������ж���
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
			//			//			Log.v(TAG, "gif����Listener");
			//			gifView.setAnimationListener(mListener);
			//			image = (ImageView) gifView;
		} else
		{
			// ��gifͼƬ�������ͼƬ������Ҫ�����������
			image = createGeneralImageView(filepath);
		}

		return image;
	}

	private ImageView createGeneralImageView(String filepath)
	{
		ImageView image = null;

		if (animationType >= XML_ANIM_TYPE_COUNT)// ��Ҷ�������������view
		{
			shutterImageView = new ShutterImageView(this);
			if ((animationType % 2) == 0)// ���ѡ��ˮƽ��ֱ
			{
				shutterImageView.setOrientation(ShutterImageView.HORIZENTAL);
			}

			shutterImageView.setAnimationListener(mListener);
			image = (ShutterImageView) shutterImageView;
		} else
		{
			image = new ImageView(this);
		}

		image.setImageBitmap(decodeImage(filepath)); // ����Bitmap
		image.setScaleType(ScaleType.CENTER);// ������ʾ�������Ŵ󣬱���Сͼ�񱻷Ŵ�
		return image;
	}

	private Bitmap decodeImage(String filePath)
	{
		// �ȵõ�ͼ��Ĵ�С
		int inSampleSize = 1; // ������С
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);// ֻ�ǽ���õ���С
		int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;

		// �Ƚ����²������õ���С�ʺ���Ļ��С��ͼ��
		if (imageWidth > mScreenWidth || imageHeight > mScreenHeight)
		{
			int widthSampleSize = (int) ((float) (imageWidth) / (float) mScreenWidth);
			int heightSampleSize = (int) ((float) (imageHeight) / (float) mScreenHeight);
			inSampleSize = widthSampleSize > heightSampleSize ? widthSampleSize
					: heightSampleSize;
		}
		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		options.inJustDecodeBounds = false;// ��Ǵ˴ν��벻�ǵõ�ͼƬ��С,���ǵõ�����
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

		// �����������ʾerrorͼƬ
		if (bitmap == null)
		{
			return BitmapFactory.decodeResource(getResources(),
					R.drawable.error);
		}

		// �ж��Ƿ���Ȼ����Ļ��С���󣬾���Ҫ���þ�ȷ��С��ʽ
		imageWidth = bitmap.getWidth();
		imageHeight = bitmap.getHeight();
		if (imageWidth > mScreenWidth || imageHeight > mScreenHeight)
		{
			// ������С�ı���ֵ
			float xScale = (float) imageWidth / (float) mScreenWidth;
			float yScale = (float) imageHeight / (float) mScreenHeight;
			float scale = xScale > yScale ? (float) 1.0 / xScale : (float) 1.0
					/ yScale;

			// ׼������
			Matrix matrix = new Matrix();
			matrix.setScale(scale, scale);

			Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
					imageWidth, imageHeight, matrix, false);
			return resizedBitmap;
		}// else ����δ����ǰ��ͼƬ
		return bitmap;
	}

	private void resetFileAndAnimation(int index)
	{
		// ��ȡ�������ļ�
		if (suffixisGIF(mFilePath))
		{
			animationType = 0;
			return;
		}

		// �õ�һ�����������Ϊ����Ч������
		animationType = (int) (100 * Math.random()) % ANIM_TYPE_COUNT;
		mViewFlipper.clearAnimation();

		// ��Ҷ������Ҫ���ö���
		if (animationType >= XML_ANIM_TYPE_COUNT)
		{
			return;
		}

		// ���ñ�׼�Ķ�������ʵ��
		int key = 2 * animationType;
		mViewFlipper.setInAnimation(animations.get(key));
		mViewFlipper.setOutAnimation(animations.get(key + 1));
		animations.get(key).setAnimationListener(mListener);
	}

	private boolean suffixisGIF(String filename)
	{
		// ��ȡ�ļ���׺��������ļ�����ͬ��˵���������ļ���չ��
		String suffix = StringUtils.substringAfterLast(filename, ".");
		if (StringUtils.equals(filename, suffix))
		{
			return false;
		}
		// ���ļ���չ��ת�ɴ�д�����ж��Ƿ�ΪGIF
		suffix = StringUtils.upperCase(suffix, Locale.ENGLISH);
		return StringUtils.equals(suffix, "GIF");
	}

	private void getDisplayInfo()
	{
		// �õ���Ļ�豸��Ϣ
		Point displaySize = new Point();
		Display display = getWindowManager().getDefaultDisplay();

		// ���÷�����ƣ�ִ�����ص�API�ӿ�
		try
		{
			Class<?> displayCls = Class.forName("android.view.Display");
			Method methodGetRealSize = displayCls.getMethod("getRealSize",
					Point.class);
			methodGetRealSize.invoke(display, displaySize);
		} catch (Exception e)
		{
			e.printStackTrace();
			display.getSize(displaySize);// ��������쳣���͵õ�ȥ��Status��Ĵ�С
		}

		mScreenWidth = displaySize.x;
		mScreenHeight = displaySize.y;
	}

	private void loadAnimations()
	{
		// ���ض���
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
			shutterImageView = null;// ��Ϊ�գ��ͷ�ԭʼ�İ�Ҷ��view
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
		if (isPause)//�������ͣ״̬����Ҫ�����Լ�������
		{
			if (!(suffixisGIF(mFilePath)))
			{
				handleState();
			}
		}

		if (isPlaying == false)// ����δ���ţ��Ϳ�ʼ����
		{
			videoView.start();
			isPlaying = true;
		} else
			// �ָ�����
		{
			videoView.seekTo(playPosition);
			videoView.start();
		}

		isPause = false;// ��������
		super.onResume();
	}

	@Override
	public void onPause()
	{
		// ִ����ͣ���ŵĹ���
		isPause = true;

		if (videoView.isPlaying())// ������ڲ�����Ƶ������ͣ����
		{
			playPosition = videoView.getCurrentPosition();
			videoView.pause();
		}

		super.onPause();
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

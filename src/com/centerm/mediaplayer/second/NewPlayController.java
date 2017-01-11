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
 * ����boc_ad.xml�����������б�Ȼ����Ʋ����ĸ�Acitivity��App��ִ�в��š�
 * 
 */
public class NewPlayController extends CommonActivity
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(MainActivity.TAG, "playcontroller start");
		super.onCreate(savedInstanceState);
		taskList = new LinkedList<NewTask>();

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
		}// TODO: else �����Ķ�������Ϊδ���Ĺ�����չ
	}

	/**
	 * ִ�в�������
	 * 
	 * @param task
	 *            ��ִ�е�����
	 */
	private void doPlayTask(NewTask task)
	{
		Intent intent = null;

		// ������Դ������ִ�в���
		switch (task.getFileType())
		{
		case FILE_TYPE_IMAGE:// ����ͼƬ
			intent = new Intent();
			intent.setClass(this, NewImagePlayer.class);
			intent.putExtra("newfile", task.getNewFile());
			intent.putExtra("loop", bPlayLoop);// TODO:�Ƿ�ѭ������
			startActivityForResult(intent, REQUEST_CODE_PLAY_IMAGE);
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

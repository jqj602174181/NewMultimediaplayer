package com.centerm.mediaplayer.image;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

import com.centerm.commons.utils.FileUtils;
import com.centerm.commons.utils.StringUtils;
import com.centerm.mediaplayer.CommonActivity;
import com.centerm.mediaplayer.MainActivity;
import com.centerm.mediaplayer.R;
import com.centerm.mediaplayer.second.NewImagePlayer;

public class ImagePlayerEx extends CommonActivity {
	private final static int XML_ANIM_TYPE_COUNT = 5;// ����XMLʵ�ֵĶ���Ч���ĸ���
	private final static int ANIM_TYPE_COUNT = XML_ANIM_TYPE_COUNT + 2; // ����Ч���ĸ��������ϰ�Ҷ����2��Ч����
	private final static int DEF_INTERVAL_TIME = 5; // Ĭ��ͼƬ�л�ʱ��Ϊ5��һ��

	private int mScreenWidth = 1024; // ��Ļ���
	private int mScreenHeight = 600; // ��Ļ�߶�
	private int mIntervalTimes = DEF_INTERVAL_TIME * 1000; // ͼƬ�л��ļ��ʱ�䣬TODO:δ��Ӧ�ø������������¸�ֵ
	private ViewFlipper mViewFlipper = null; // ����ͼƬ������
	private ArrayList<String> fileList; // ͼƬ�ļ��б�
	private ShutterImageView shutterImageView = null;// ��Ҷ����ͼ

	private int animationType = 0; // ͼƬ����Ч������
	private boolean isPause = false; // �Ƿ�������ͣ״̬

	private boolean bPlayLoop = false; // �Ƿ�ѭ������
	private static SparseArray<Animation> animations = null;// ������Ч��,
	private GifImageView gifView = null;

	private ImageView mImageView = null;
	private HandlerThread mPlayerHandlerThread = null;
	private Handler mPlayerHandler = null;
	private Handler mPlayerUIHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageplayer);

		// �õ�ͼƬ�����б�
		Intent intent = getIntent();
		fileList = intent.getStringArrayListExtra("filelist");
		bPlayLoop = intent.getBooleanExtra("loop", false);	// ��ȡѭ�����ŵ����ò���
		if (fileList == null || fileList.size() == 0) {		// �����ͼƬ����ֱ�ӽ���
			finish();
			return;
		}		

		getDisplayInfo();	// �õ���Ļ��С��Ϣ
		loadAnimations();	// ���ض�����Դ	
		initViews();		// ��ʼ�����ͼƬ�ؼ�

		//���������߳�
		mPlayerUIHandler = new PlayerUIHandler();
		mPlayerHandlerThread = new HandlerThread(MainActivity.TAG);//���������߳�
		mPlayerHandlerThread.start();
		mPlayerHandler = new PlayerHandler(mPlayerHandlerThread.getLooper());
		//Message msg = mPlayerHandler.obtainMessage(PlayerHandler.MSG_PLAY_NEXT);
		//mPlayerHandler.sendMessage(msg);
	}

	@SuppressLint("HandlerLeak") 
	private class PlayerUIHandler extends Handler {
		public static final int MSG_PLAY_END = 0;
		public static final int MSG_PLAY_SHOW = 1;
		@Override
		public void handleMessage(Message msg) {
			//Log.i(MainActivity.TAG, "PlayerUIHandler:" + msg.what);
			switch (msg.what) {
			case MSG_PLAY_END:
				finish();
				break;
			case MSG_PLAY_SHOW:
				resetFileAndAnimation(); // ���·���ͼƬ�ļ��Ͷ�������
				mViewFlipper.addView(mImageView);
				mViewFlipper.showNext();// ��ʾ��һ��ͼƬ

				if (mViewFlipper.getChildCount() > 1) {
					// ���ǰһ��ͼƬ����Դ
					mImageView = (ImageView) mViewFlipper.getChildAt(0);
					mImageView.setImageResource(0);
					mViewFlipper.removeViewAt(0);
				}

				msg = mPlayerHandler.obtainMessage(PlayerHandler.MSG_PLAY_NEXT);
				mPlayerHandler.sendMessageDelayed(msg, mIntervalTimes);
				break;
			}
		}
	}

	@SuppressLint("HandlerLeak") 
	private class PlayerHandler extends Handler {
		public static final int MSG_PLAY_NEXT = 0;
		private int fileIndex = -1; // ���ڲ��ŵ��ļ����б��е�����
		private String mPicturePath = null;
		public PlayerHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			//Log.i(MainActivity.TAG, "PlayerHandler:" + msg.what);
			switch (msg.what) {
			case MSG_PLAY_NEXT:
				if (isPause == true) {
					break;
				}
				fileIndex++;
				// ���������һ��ͼƬ����û��ѭ�����ŵ�Ҫ�󣬾ͽ�����ǰactivity
				if ((fileList.size() == fileIndex) && 
						(bPlayLoop == false)) {
					msg = mPlayerUIHandler.obtainMessage(PlayerUIHandler.MSG_PLAY_END);
					mPlayerUIHandler.sendMessage(msg);
					break;
				}

				//��ȡ�������ļ�·��
				mPicturePath = fileList.get(fileIndex);

				//�ļ������������activity
				if (!FileUtils.isFileExit(mPicturePath)) {
					msg = mPlayerUIHandler.obtainMessage(PlayerUIHandler.MSG_PLAY_END);
					mPlayerUIHandler.sendMessage(msg);
					break;
				}

				// ������һ��ͼƬ��view
				mImageView = createImageView(mPicturePath);
				msg = mPlayerUIHandler.obtainMessage(PlayerUIHandler.MSG_PLAY_SHOW);
				mPlayerUIHandler.sendMessage(msg);
				break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPlayerHandlerThread.quit();	//�˳������߳�
		mPlayerHandlerThread = null;
		mPlayerHandler = null;
		mPlayerUIHandler = null;
		cleanup();
		setResult(0);					// ���߲���controller�����Ž���
	}

	@Override
	public void onResume() {
		super.onResume();
		isPause = false;
		Message msg = mPlayerHandler.obtainMessage(PlayerHandler.MSG_PLAY_NEXT);
		mPlayerHandler.sendMessage(msg);
	}

	@Override
	public void onPause() {
		super.onPause();
		isPause = true;
	}

	/**
	 * ִ��ͼƬ�������
	 */
	private void cleanup() {
		// ���ͼƬ����
		if (mViewFlipper != null) {
			// ������һ��ͼƬ����Դ
			ImageView imageView = (ImageView) mViewFlipper.getChildAt(0);
			if (imageView != null) {
				imageView.setImageResource(0);
				mViewFlipper.removeViewAt(0);
			}
		}
	}

	/**
	 * ��ʼ�����ͼƬ�ؼ�
	 */
	private void initViews() {
		//����ͼ��view�����ӵ�ָ����flipper��
		mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
	}

	/**
	 * ��ȡ��ʾ�豸��Ϣ��������Ⱥ͸߶�
	 */
	private void getDisplayInfo() {
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

	/**
	 * ���ض�����Դ
	 */
	private void loadAnimations() {
		// ���ض���
		if (animations == null) {
			animations = new SparseArray<Animation>();
			for (int i = 0; i < XML_ANIM_TYPE_COUNT * 2; i++) {
				Animation animation = AnimationUtils.loadAnimation(this,
						R.anim.push_left_in + i);
				animations.put(i, animation);
			}
		}		
	}

	/**
	 * ����ͼƬ���ſؼ�ImageView
	 * 
	 * @param index
	 *            ͼƬ������
	 * @return ͼƬ���ſؼ�
	 */
	private ImageView createImageView(String filepath) {
		ImageView image = null;

		if (suffixisGIF(filepath)) {	//gifͼƬ
			//�ݲ�֧��
			gifView = new GifImageView( ImagePlayerEx.this );
			gifView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			gifView.setScaleType(ImageView.ScaleType.FIT_XY);
			gifView.setImageURI( Uri.fromFile( new File(filepath)) );
			image = (ImageView) gifView;
		}
		else {
			// ��gifͼƬ�������ͼƬ������Ҫ�����������
			image = createGeneralImageView(filepath);
		}
		return image;
	}

	/**
	 * �����gif�������ͼƬ����ͼ������
	 * @param filepath �ļ�ȫ·��
	 * @return ͼ����ͼ
	 */
	private ImageView createGeneralImageView(String filepath) {
		ImageView image = null;

		if (animationType >= XML_ANIM_TYPE_COUNT) {		// ��Ҷ�������������view
			shutterImageView = new ShutterImageView(this);
			if ((animationType % 2) == 0) {				// ���ѡ��ˮƽ��ֱ
				shutterImageView.setOrientation(ShutterImageView.HORIZENTAL);
			}

			shutterImageView.setAnimationListener(mListener);
			image = (ShutterImageView) shutterImageView;
		}
		else {
			image = new ImageView(this);
		}

		image.setImageBitmap(decodeImage(filepath)); // ����Bitmap
		image.setScaleType(ScaleType.CENTER);// ������ʾ�������Ŵ󣬱���Сͼ�񱻷Ŵ�
		return image;
	}

	/**
	 * �ж��ļ��Ƿ���gifͼƬ
	 * @param filename �ļ���
	 * @return true��ʾ�ǣ�false��ʾ��
	 */
	private boolean suffixisGIF(String filename){
		// ��ȡ�ļ���׺��������ļ�����ͬ��˵���������ļ���չ��
		String suffix = StringUtils.substringAfterLast(filename, ".");
		if (StringUtils.equals(filename, suffix)) {
			return false;
		}

		// ���ļ���չ��ת�ɴ�д�����ж��Ƿ�ΪGIF
		suffix = StringUtils.upperCase(suffix, Locale.ENGLISH);
		return StringUtils.equals(suffix, "GIF");
	}

	/**
	 * ����ͼ��õ�bitmap�����ͼ�����Ҫ������С
	 * 
	 * @param filePath
	 *            �ļ�·��
	 * @return bitmap
	 */
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

	/**
	 * ѡ������ŵ�ͼƬ�Ͷ���
	 */
	private void resetFileAndAnimation()
	{
		// �õ�һ�����������Ϊ����Ч������
		animationType = (int) (100 * Math.random()) % ANIM_TYPE_COUNT;
		mViewFlipper.clearAnimation();

		// ��Ҷ������Ҫ���ö���
		if (animationType >= XML_ANIM_TYPE_COUNT)
		{
			//			Log.i(TAG, "��Ҷ��");
			return;
		}

		// ���ñ�׼�Ķ�������ʵ��
		int key = 2 * animationType;
		mViewFlipper.setInAnimation(animations.get(key));
		mViewFlipper.setOutAnimation(animations.get(key + 1));
		animations.get(key).setAnimationListener(mListener);
	}

	/**
	 * ���嶯������֪ͨ����
	 */
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

			// ���ͼƬδ������ɣ��͹��ؼ��һ��ʱ���������ŵ���Ϣ
			//Message msg = mPlayerHandler.obtainMessage(PlayerHandler.MSG_PLAY_NEXT);
			//mPlayerHandler.sendMessageDelayed(msg, mIntervalTimes);
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
}

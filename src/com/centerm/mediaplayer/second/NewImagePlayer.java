package com.centerm.mediaplayer.second;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ViewFlipper;

import com.centerm.mediaplayer.CommonActivity;
import com.centerm.commons.utils.StringUtils;
import com.centerm.mediaplayer.R;
import com.centerm.mediaplayer.image.ShutterImageView;
import com.centerm.mediaplayer.three.MenuFxService;

/**
 * ͼƬ����
 */
public class NewImagePlayer extends CommonActivity implements OnTouchListener, OnGestureListener
{
	private final static int MSG_PLAY_NEXT = 990; // ������һ��ͼƬ
	private final static int XML_ANIM_TYPE_COUNT = 5;// ����XMLʵ�ֵĶ���Ч���ĸ���
	private final static int ANIM_TYPE_COUNT = XML_ANIM_TYPE_COUNT + 2; // ����Ч���ĸ��������ϰ�Ҷ����2��Ч����
	private final static int DEF_INTERVAL_TIME = 5; // Ĭ��ͼƬ�л�ʱ��Ϊ5��һ��

	private int mScreenWidth = 1024; // ��Ļ���
	private int mScreenHeight = 600; // ��Ļ�߶�
	private int mIntervalTimes = DEF_INTERVAL_TIME * 1000; // ͼƬ�л��ļ��ʱ�䣬TODO:δ��Ӧ�ø������������¸�ֵ
	private ViewFlipper mViewFlipper = null; // ����ͼƬ������
	private NewFile newFile; // ͼƬ�ļ��б�
	private ShutterImageView shutterImageView = null;// ��Ҷ����ͼ
	private int fileIndex = 0; // ���ڲ��ŵ��ļ����б��е�����
	private int animationType = 0; // ͼƬ����Ч������
	private boolean isPause = false; // �Ƿ�������ͣ״̬

	private boolean bPlayLoop = false; // �Ƿ�ѭ������
	private static SparseArray<Animation> animations = null;// ������Ч��,

	private long originalTime = 0, currentTime = 0;
	private String mPicturePath = null;			 //��ǰ���ڲ���״̬��ͼƬ·����
	private GifImageView gifView = null;
	private GestureDetector mDetector; 

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageplayer);

		// �õ�ͼƬ�����б�
		Intent intent = getIntent();
		newFile = (NewFile)intent.getSerializableExtra("newfile");
		bPlayLoop = intent.getBooleanExtra("loop", false);// ��ȡѭ�����ŵ����ò���
		if (newFile == null)// �����ͼƬ����ֱ�ӽ���
		{
			finish();
			return;
		}		

		getDisplayInfo();// �õ���Ļ��С��Ϣ
		loadAnimations();//���ض�����Դ		
		initImages();// ��ʼ�����ͼƬ�ؼ��������ص�һ��ͼƬ
	}

	/**
	 * ���ض�����Դ
	 */
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

	/**
	 * ���ص�һ��ͼƬ
	 */
	private void initImages()
	{
		//����ͼ��view�����ӵ�ָ����flipper��
		mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		mViewFlipper.setLongClickable(true);
		mViewFlipper.setOnTouchListener(this); //ע��OnTouch������
		mDetector = new GestureDetector(this); 

		resetFileAndAnimation(0);//ѡ���ļ��Ͷ�������
		ImageView imageView = createImageView(mPicturePath);
		mViewFlipper.addView(imageView);

		//��¼������ͼƬ��ʾ��ʱ��
		originalTime = System.currentTimeMillis();

		// �����һ��ͼƬ��gif�Ͳ��÷���Ϣ����Ϊgif��ͨ��AnimationEnd֪ͨ
		//		if (suffixisGIF(mPicturePath))
		//		{
		//			return;
		//		}//else ���´���Ϊelse����

		handleState();//���һ��ʱ��󣬼�ⲥ��״̬
	}

	/**
	 * ���һ��ʱ��󣬼�ⲥ��״̬�����δ�Զ��л�����
	 */
	private void handleState()
	{
		//		String mediaPlayerConfig = "/mnt/internal_sd/config/multimedia.ini";
		//		RWIniClass.resetIniMap(mediaPlayerConfig);
		//		mIntervalTimes = RWIniClass.ReadIniInt(mediaPlayerConfig, "Picture", "delaytime", DEF_INTERVAL_TIME);
		mIntervalTimes = newFile.getInterval() * 1000;
		Message msg = mHandler.obtainMessage(MSG_PLAY_NEXT);
		mHandler.sendMessageDelayed(msg, mIntervalTimes);
	}

	@Override
	protected void onStart()
	{
		//		Log.v(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		//		Log.v(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onResume()
	{
		//		Log.v(TAG, "onResume");
		if (isPause)//�������ͣ״̬����Ҫ�����Լ�������
		{
			if (!(suffixisGIF(mPicturePath)))
			{
				handleState();
			}
		}

		isPause = false;// ��������
		super.onResume();
	}

	@Override
	public void onPause()
	{
		// ִ����ͣ���ŵĹ���
		//		Log.v(TAG, "onPause");
		isPause = true;

		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		cleanup();
		setResult(0);// ���߲���controller�����Ž���
	}

	/**
	 * ִ��ͼƬ�������
	 */
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

	/**
	 * ��ȡ��ʾ�豸��Ϣ��������Ⱥ͸߶�
	 */
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

	/**
	 * ��һ�ֲ��Ž���ʱ���ã��ж��Ƿ����ѭ������
	 * 
	 * @return true��ʾ�������ţ�false��ʾ������һ��
	 */
	private boolean isPlayLoop()
	{
		if (bPlayLoop)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * �¼�����
	 */
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
			if ((currentTime - originalTime) < mIntervalTimes-5)
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
				finish();
				return;
			}

			// ׼��������һ��ͼƬ
			//			fileIndex = (fileIndex + 1) % fileList.size();
			//			resetFileAndAnimation(fileIndex); // ���·���ͼƬ�ļ��Ͷ�������
			//			
			//			if(!FileUtils.isFileExit(mPicturePath))
			//			{
			//				mHandler.removeMessages(MSG_PLAY_NEXT);
			//				finish();
			//				return;
			//			}
			//
			//			// ������һ��ͼƬ��view������ʾ
			//			ImageView imageView = createImageView(mPicturePath);
			//			mViewFlipper.addView(imageView);
			//			mViewFlipper.showNext();// ��ʾ��һ��ͼƬ
			//
			//			// ���ǰһ��ͼƬ����Դ
			//			imageView = (ImageView) mViewFlipper.getChildAt(0);
			//			cleanImageView(imageView);
			//			mViewFlipper.removeViewAt(0);
			//
			//			// ��ȡ��ǰʱ��
			//			originalTime = System.currentTimeMillis();
		}
	};

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

	/**
	 * ����ͼƬ���ſؼ�ImageView
	 * 
	 * @param index
	 *            ͼƬ������
	 * @return ͼƬ���ſؼ�
	 */
	private ImageView createImageView(String filepath)
	{
		ImageView image = null;

		if (suffixisGIF(filepath))//gifͼƬ
		{
			// gifͼƬ���������κζ�������Ϊ�������ж���
			mViewFlipper.clearAnimation();
			mViewFlipper.setInAnimation(animations.get(0));
			mViewFlipper.setOutAnimation(animations.get(1));
			//			gifView = new GifImageView(this, mPicturePath);
			gifView = new GifImageView( NewImagePlayer.this );
			gifView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			gifView.setScaleType(ImageView.ScaleType.FIT_XY);
			gifView.setImageURI( Uri.fromFile( new File(filepath)) );

			//			Log.v(TAG, "gif����Listener");
			//			gifView.setAnimationListener(mListener);
			image = (ImageView) gifView;
		} else
		{
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

	/**
	 * �ж��ļ��Ƿ���gifͼƬ
	 * @param filename �ļ���
	 * @return true��ʾ�ǣ�false��ʾ��
	 */
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
	 * �ͷ�ͼƬ���ſؼ�����Դ
	 * 
	 * @param view
	 *            ͼƬ���ſؼ�
	 */
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

	/**
	 * ѡ������ŵ�ͼƬ�Ͷ���
	 */
	private void resetFileAndAnimation(int index)
	{
		// ��ȡ�������ļ�
		mPicturePath = newFile.getName();
		if (suffixisGIF(mPicturePath))
		{
			//Log.i(TAG, "gif����Ҫ���ö���");
			animationType = 0;
			return;
		}

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

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.i("NewImagePlayer", "onFling Happened!");    
		if (e1.getX() - e2.getX() > 10) {    
			Intent intent = new Intent(NewImagePlayer.this, MenuFxService.class);
			NewImagePlayer.this.startService(intent);
			return true;    
		} else if (e1.getX() - e2.getX() < -10) {  
			Intent intent = new Intent(NewImagePlayer.this, MenuFxService.class);
			NewImagePlayer.this.startService(intent);
			return true;    
		}    
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.viewFlipper) {  
			return mDetector.onTouchEvent(event); //���¼�����GestureDetector.onTouchEvent()  
		}  
		return false; 
	}

}

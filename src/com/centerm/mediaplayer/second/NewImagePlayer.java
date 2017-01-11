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
 * 图片播放
 */
public class NewImagePlayer extends CommonActivity implements OnTouchListener, OnGestureListener
{
	private final static int MSG_PLAY_NEXT = 990; // 播放下一张图片
	private final static int XML_ANIM_TYPE_COUNT = 5;// 采用XML实现的动画效果的个数
	private final static int ANIM_TYPE_COUNT = XML_ANIM_TYPE_COUNT + 2; // 动画效果的个数（加上百叶窗的2种效果）
	private final static int DEF_INTERVAL_TIME = 5; // 默认图片切换时间为5秒一张

	private int mScreenWidth = 1024; // 屏幕宽度
	private int mScreenHeight = 600; // 屏幕高度
	private int mIntervalTimes = DEF_INTERVAL_TIME * 1000; // 图片切换的间隔时间，TODO:未来应该根据配置来更新该值
	private ViewFlipper mViewFlipper = null; // 播放图片的容器
	private NewFile newFile; // 图片文件列表
	private ShutterImageView shutterImageView = null;// 百叶窗视图
	private int fileIndex = 0; // 正在播放的文件在列表中的索引
	private int animationType = 0; // 图片动画效果类型
	private boolean isPause = false; // 是否处理于暂停状态

	private boolean bPlayLoop = false; // 是否循环播放
	private static SparseArray<Animation> animations = null;// 动画的效果,

	private long originalTime = 0, currentTime = 0;
	private String mPicturePath = null;			 //当前正在播放状态的图片路径名
	private GifImageView gifView = null;
	private GestureDetector mDetector; 

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageplayer);

		// 得到图片播放列表
		Intent intent = getIntent();
		newFile = (NewFile)intent.getSerializableExtra("newfile");
		bPlayLoop = intent.getBooleanExtra("loop", false);// 获取循环播放的配置参数
		if (newFile == null)// 如果无图片，则直接结束
		{
			finish();
			return;
		}		

		getDisplayInfo();// 得到屏幕大小信息
		loadAnimations();//加载动作资源		
		initImages();// 初始化广告图片控件，并加载第一张图片
	}

	/**
	 * 加载动作资源
	 */
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

	/**
	 * 加载第一张图片
	 */
	private void initImages()
	{
		//创建图像view，并加到指定的flipper中
		mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		mViewFlipper.setLongClickable(true);
		mViewFlipper.setOnTouchListener(this); //注册OnTouch监听器
		mDetector = new GestureDetector(this); 

		resetFileAndAnimation(0);//选择文件和动画类型
		ImageView imageView = createImageView(mPicturePath);
		mViewFlipper.addView(imageView);

		//记录最后更新图片显示的时间
		originalTime = System.currentTimeMillis();

		// 如果第一张图片是gif就不用发消息，因为gif会通过AnimationEnd通知
		//		if (suffixisGIF(mPicturePath))
		//		{
		//			return;
		//		}//else 以下代码为else处理

		handleState();//间隔一定时间后，检测播放状态
	}

	/**
	 * 间隔一定时间后，检测播放状态，如果未自动切换，就
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
		if (isPause)//如果是暂停状态，需要启动以继续播放
		{
			if (!(suffixisGIF(mPicturePath)))
			{
				handleState();
			}
		}

		isPause = false;// 继续播放
		super.onResume();
	}

	@Override
	public void onPause()
	{
		// 执行暂停播放的功能
		//		Log.v(TAG, "onPause");
		isPause = true;

		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		cleanup();
		setResult(0);// 告诉播放controller，播放结束
	}

	/**
	 * 执行图片缓存清除
	 */
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

	/**
	 * 获取显示设备信息，包括宽度和高度
	 */
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

	/**
	 * 当一轮播放结束时调用，判断是否进行循环播放
	 * 
	 * @return true表示结束播放，false表示继续下一轮
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
	 * 事件处理
	 */
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
			if ((currentTime - originalTime) < mIntervalTimes-5)
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
				finish();
				return;
			}

			// 准备播放下一张图片
			//			fileIndex = (fileIndex + 1) % fileList.size();
			//			resetFileAndAnimation(fileIndex); // 重新分配图片文件和动画类型
			//			
			//			if(!FileUtils.isFileExit(mPicturePath))
			//			{
			//				mHandler.removeMessages(MSG_PLAY_NEXT);
			//				finish();
			//				return;
			//			}
			//
			//			// 加载下一张图片的view，并显示
			//			ImageView imageView = createImageView(mPicturePath);
			//			mViewFlipper.addView(imageView);
			//			mViewFlipper.showNext();// 显示下一张图片
			//
			//			// 清除前一张图片的资源
			//			imageView = (ImageView) mViewFlipper.getChildAt(0);
			//			cleanImageView(imageView);
			//			mViewFlipper.removeViewAt(0);
			//
			//			// 获取当前时间
			//			originalTime = System.currentTimeMillis();
		}
	};

	/**
	 * 定义动画结束通知函数
	 */
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

			// 如果图片未播放完成，就挂载间隔一定时间后继续播放的消息
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
	 * 创建图片播放控件ImageView
	 * 
	 * @param index
	 *            图片的索引
	 * @return 图片播放控件
	 */
	private ImageView createImageView(String filepath)
	{
		ImageView image = null;

		if (suffixisGIF(filepath))//gif图片
		{
			// gif图片，不采用任何动画，因为它自身有动画
			mViewFlipper.clearAnimation();
			mViewFlipper.setInAnimation(animations.get(0));
			mViewFlipper.setOutAnimation(animations.get(1));
			//			gifView = new GifImageView(this, mPicturePath);
			gifView = new GifImageView( NewImagePlayer.this );
			gifView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			gifView.setScaleType(ImageView.ScaleType.FIT_XY);
			gifView.setImageURI( Uri.fromFile( new File(filepath)) );

			//			Log.v(TAG, "gif设置Listener");
			//			gifView.setAnimationListener(mListener);
			image = (ImageView) gifView;
		} else
		{
			// 除gif图片外的所有图片处理，需要采用随机动画
			image = createGeneralImageView(filepath);
		}

		return image;
	}

	/**
	 * 处理除gif外的所有图片的视图创建，
	 * @param filepath 文件全路径
	 * @return 图像视图
	 */
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

	/**
	 * 判断文件是否是gif图片
	 * @param filename 文件名
	 * @return true表示是，false表示否
	 */
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

	/**
	 * 解码图像得到bitmap，如果图像过大，要进行缩小
	 * 
	 * @param filePath
	 *            文件路径
	 * @return bitmap
	 */
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

	/**
	 * 释放图片播放控件的资源
	 * 
	 * @param view
	 *            图片播放控件
	 */
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

	/**
	 * 选择待播放的图片和动画
	 */
	private void resetFileAndAnimation(int index)
	{
		// 获取待播放文件
		mPicturePath = newFile.getName();
		if (suffixisGIF(mPicturePath))
		{
			//Log.i(TAG, "gif不需要设置动画");
			animationType = 0;
			return;
		}

		// 得到一个随机数，作为动画效果类型
		animationType = (int) (100 * Math.random()) % ANIM_TYPE_COUNT;
		mViewFlipper.clearAnimation();

		// 百叶窗不需要设置动画
		if (animationType >= XML_ANIM_TYPE_COUNT)
		{
			//			Log.i(TAG, "百叶窗");
			return;
		}

		// 采用标准的动画方法实现
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
			return mDetector.onTouchEvent(event); //将事件交给GestureDetector.onTouchEvent()  
		}  
		return false; 
	}

}

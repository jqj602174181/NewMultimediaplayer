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
	private final static int XML_ANIM_TYPE_COUNT = 5;// 采用XML实现的动画效果的个数
	private final static int ANIM_TYPE_COUNT = XML_ANIM_TYPE_COUNT + 2; // 动画效果的个数（加上百叶窗的2种效果）
	private final static int DEF_INTERVAL_TIME = 5; // 默认图片切换时间为5秒一张

	private int mScreenWidth = 1024; // 屏幕宽度
	private int mScreenHeight = 600; // 屏幕高度
	private int mIntervalTimes = DEF_INTERVAL_TIME * 1000; // 图片切换的间隔时间，TODO:未来应该根据配置来更新该值
	private ViewFlipper mViewFlipper = null; // 播放图片的容器
	private ArrayList<String> fileList; // 图片文件列表
	private ShutterImageView shutterImageView = null;// 百叶窗视图

	private int animationType = 0; // 图片动画效果类型
	private boolean isPause = false; // 是否处理于暂停状态

	private boolean bPlayLoop = false; // 是否循环播放
	private static SparseArray<Animation> animations = null;// 动画的效果,
	private GifImageView gifView = null;

	private ImageView mImageView = null;
	private HandlerThread mPlayerHandlerThread = null;
	private Handler mPlayerHandler = null;
	private Handler mPlayerUIHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageplayer);

		// 得到图片播放列表
		Intent intent = getIntent();
		fileList = intent.getStringArrayListExtra("filelist");
		bPlayLoop = intent.getBooleanExtra("loop", false);	// 获取循环播放的配置参数
		if (fileList == null || fileList.size() == 0) {		// 如果无图片，则直接结束
			finish();
			return;
		}		

		getDisplayInfo();	// 得到屏幕大小信息
		loadAnimations();	// 加载动作资源	
		initViews();		// 初始化广告图片控件

		//启动播放线程
		mPlayerUIHandler = new PlayerUIHandler();
		mPlayerHandlerThread = new HandlerThread(MainActivity.TAG);//升级处理线程
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
				resetFileAndAnimation(); // 重新分配图片文件和动画类型
				mViewFlipper.addView(mImageView);
				mViewFlipper.showNext();// 显示下一张图片

				if (mViewFlipper.getChildCount() > 1) {
					// 清除前一张图片的资源
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
		private int fileIndex = -1; // 正在播放的文件在列表中的索引
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
				// 如果是最后的一张图片，且没有循环播放的要求，就结束当前activity
				if ((fileList.size() == fileIndex) && 
						(bPlayLoop == false)) {
					msg = mPlayerUIHandler.obtainMessage(PlayerUIHandler.MSG_PLAY_END);
					mPlayerUIHandler.sendMessage(msg);
					break;
				}

				//获取待播放文件路径
				mPicturePath = fileList.get(fileIndex);

				//文件不存在则结束activity
				if (!FileUtils.isFileExit(mPicturePath)) {
					msg = mPlayerUIHandler.obtainMessage(PlayerUIHandler.MSG_PLAY_END);
					mPlayerUIHandler.sendMessage(msg);
					break;
				}

				// 加载下一张图片的view
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
		mPlayerHandlerThread.quit();	//退出播放线程
		mPlayerHandlerThread = null;
		mPlayerHandler = null;
		mPlayerUIHandler = null;
		cleanup();
		setResult(0);					// 告诉播放controller，播放结束
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
	 * 执行图片缓存清除
	 */
	private void cleanup() {
		// 清除图片缓存
		if (mViewFlipper != null) {
			// 清除最后一张图片的资源
			ImageView imageView = (ImageView) mViewFlipper.getChildAt(0);
			if (imageView != null) {
				imageView.setImageResource(0);
				mViewFlipper.removeViewAt(0);
			}
		}
	}

	/**
	 * 初始化广告图片控件
	 */
	private void initViews() {
		//创建图像view，并加到指定的flipper中
		mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
	}

	/**
	 * 获取显示设备信息，包括宽度和高度
	 */
	private void getDisplayInfo() {
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
	 * 加载动作资源
	 */
	private void loadAnimations() {
		// 加载动画
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
	 * 创建图片播放控件ImageView
	 * 
	 * @param index
	 *            图片的索引
	 * @return 图片播放控件
	 */
	private ImageView createImageView(String filepath) {
		ImageView image = null;

		if (suffixisGIF(filepath)) {	//gif图片
			//暂不支持
			gifView = new GifImageView( ImagePlayerEx.this );
			gifView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			gifView.setScaleType(ImageView.ScaleType.FIT_XY);
			gifView.setImageURI( Uri.fromFile( new File(filepath)) );
			image = (ImageView) gifView;
		}
		else {
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
	private ImageView createGeneralImageView(String filepath) {
		ImageView image = null;

		if (animationType >= XML_ANIM_TYPE_COUNT) {		// 百叶窗，采用特殊的view
			shutterImageView = new ShutterImageView(this);
			if ((animationType % 2) == 0) {				// 随机选择水平或垂直
				shutterImageView.setOrientation(ShutterImageView.HORIZENTAL);
			}

			shutterImageView.setAnimationListener(mListener);
			image = (ShutterImageView) shutterImageView;
		}
		else {
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
	private boolean suffixisGIF(String filename){
		// 获取文件后缀，如果与文件名相同，说明不存在文件扩展名
		String suffix = StringUtils.substringAfterLast(filename, ".");
		if (StringUtils.equals(filename, suffix)) {
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
	 * 选择待播放的图片和动画
	 */
	private void resetFileAndAnimation()
	{
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

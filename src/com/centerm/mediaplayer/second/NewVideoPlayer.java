package com.centerm.mediaplayer.second;

import com.centerm.mediaplayer.MainActivity;
import com.centerm.mediaplayer.R;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.VideoView;
import android.widget.MediaController;
import com.centerm.mediaplayer.CommonActivity;
import com.centerm.mediaplayer.three.MenuFxService;

public class NewVideoPlayer extends CommonActivity implements OnGestureListener
{
	private static final String TAG = MainActivity.TAG;

	private int fileIndex = 0; // 正在播放的文件在列表中的索引
	private NewFile newfile; // 视频文件列表
	private boolean isPlaying = false; // 是否正在播放
	private int playPosition = 0; // 在暂停时，用于记录暂停的位置，以便恢复播放
	private VideoView videoView; // 视频视图
	private MediaController mediaController;
	private GestureDetector mDetector; 

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.videoplayer);

		// 得到图片播放列表
		Intent intent = getIntent();
		newfile = (NewFile)intent.getSerializableExtra("newfile");
		if (newfile == null)// 如果无图片，则直接结束
		{
			finish();
			return;
		}	

		// 准备开始播放视频
		videoView = (VideoView) findViewById(R.id.videoView);
		mDetector = new GestureDetector(this); 

		mediaController = new MediaController(this);
		//		mediaController.setPadding(0,0,0,100);
		videoView.setMediaController(mediaController);
		videoView.setVideoPath(newfile.getName());
		//videoView.requestFocus();

		// 注册视频播放结束时的监听器
		videoView
		.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				playNext();// 播放下一个
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
	}

	@Override
	public void onResume()
	{
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

		super.onResume();
	}

	@Override
	public void onPause()
	{
		if (videoView.isPlaying())// 如果正在播放视频，就暂停播放
		{
			playPosition = videoView.getCurrentPosition();
			videoView.pause();
		}

		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		setResult( 0 );//告诉播放controller，播放结束
		super.onDestroy();
	}

	/**
	 * 播放下一个视频
	 */
	public void playNext()
	{
		finish();
		//		fileIndex++;
		//		if (fileIndex == fileList.size())// 全部都播放结束，就结束当前activity
		//		{
		//			finish();
		//		} else
		//		// 开始播放下一个视频
		//		{
		//			videoView.stopPlayback();// 结束上一次的播放
		//			videoView.setVideoPath(fileList.get(fileIndex));
		//			videoView.start();
		//		}
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
			Intent intent = new Intent(NewVideoPlayer.this, MenuFxService.class);
			NewVideoPlayer.this.startService(intent);
			return true;    
		} else if (e1.getX() - e2.getX() < -10) {  
			Intent intent = new Intent(NewVideoPlayer.this, MenuFxService.class);
			NewVideoPlayer.this.startService(intent);
			return true;    
		}    
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mDetector.onTouchEvent(event); //将事件交给GestureDetector.onTouchEvent() 
	}

}

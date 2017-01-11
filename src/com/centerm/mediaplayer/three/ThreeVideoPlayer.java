package com.centerm.mediaplayer.three;

import com.centerm.mediaplayer.MainActivity;
import com.centerm.mediaplayer.R;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.VideoView;
import android.widget.MediaController;
import com.centerm.mediaplayer.CommonActivity;

public class ThreeVideoPlayer extends CommonActivity
{
	private static final String TAG = MainActivity.TAG;

	private boolean isPlaying = false; // 是否正在播放
	private int playPosition = 0; // 在暂停时，用于记录暂停的位置，以便恢复播放
	private VideoView videoView; // 视频视图
	private MediaController mediaController;

	private String mPath;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.videoplayer);

		// 得到图片播放列表
		Intent intent = getIntent();
		mPath = intent.getStringExtra("mPath");
		if (mPath == null)// 如果无图片，则直接结束
		{
			finish();
			return;
		}	

		// 准备开始播放视频
		videoView = (VideoView) findViewById(R.id.videoView);

		mediaController = new MediaController(this);
		//		mediaController.setPadding(0,0,0,100);
		videoView.setMediaController(mediaController);
		videoView.setVideoPath(mPath);
		//videoView.requestFocus();

		// 注册视频播放结束时的监听器
		videoView
		.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				playNext();// 结束
			}
		});

		// 注册视频播放执行错误的监听器
		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
		{
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra)
			{
				//				playNext();// 播放下一个, TODO:发生错误时，应该告诉Controller，尝试采用未加速的播放器进行播放
				ThreeVideoPlayer.this.finish();
				return true;
			}
		});
		videoView.setFocusable(false);
		videoView.setFocusableInTouchMode(false);
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

}

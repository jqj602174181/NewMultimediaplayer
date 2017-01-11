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

	private boolean isPlaying = false; // �Ƿ����ڲ���
	private int playPosition = 0; // ����ͣʱ�����ڼ�¼��ͣ��λ�ã��Ա�ָ�����
	private VideoView videoView; // ��Ƶ��ͼ
	private MediaController mediaController;

	private String mPath;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.videoplayer);

		// �õ�ͼƬ�����б�
		Intent intent = getIntent();
		mPath = intent.getStringExtra("mPath");
		if (mPath == null)// �����ͼƬ����ֱ�ӽ���
		{
			finish();
			return;
		}	

		// ׼����ʼ������Ƶ
		videoView = (VideoView) findViewById(R.id.videoView);

		mediaController = new MediaController(this);
		//		mediaController.setPadding(0,0,0,100);
		videoView.setMediaController(mediaController);
		videoView.setVideoPath(mPath);
		//videoView.requestFocus();

		// ע����Ƶ���Ž���ʱ�ļ�����
		videoView
		.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				playNext();// ����
			}
		});

		// ע����Ƶ����ִ�д���ļ�����
		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
		{
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra)
			{
				//				playNext();// ������һ��, TODO:��������ʱ��Ӧ�ø���Controller�����Բ���δ���ٵĲ��������в���
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

		super.onResume();
	}

	@Override
	public void onPause()
	{
		if (videoView.isPlaying())// ������ڲ�����Ƶ������ͣ����
		{
			playPosition = videoView.getCurrentPosition();
			videoView.pause();
		}

		super.onPause();
	}

	/**
	 * ������һ����Ƶ
	 */
	public void playNext()
	{
		finish();
		//		fileIndex++;
		//		if (fileIndex == fileList.size())// ȫ�������Ž������ͽ�����ǰactivity
		//		{
		//			finish();
		//		} else
		//		// ��ʼ������һ����Ƶ
		//		{
		//			videoView.stopPlayback();// ������һ�εĲ���
		//			videoView.setVideoPath(fileList.get(fileIndex));
		//			videoView.start();
		//		}
	}

}

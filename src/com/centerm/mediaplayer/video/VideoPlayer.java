package com.centerm.mediaplayer.video;

import java.util.ArrayList;

import com.centerm.mediaplayer.MainActivity;
import com.centerm.mediaplayer.R;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.VideoView;
import android.widget.MediaController;
import com.centerm.mediaplayer.CommonActivity;

public class VideoPlayer extends CommonActivity
{
	private static final String TAG = MainActivity.TAG;

	private int fileIndex = 0; // ���ڲ��ŵ��ļ����б��е�����
	private ArrayList<String> fileList; // ��Ƶ�ļ��б�
	private boolean isPlaying = false; // �Ƿ����ڲ���
	private int playPosition = 0; // ����ͣʱ�����ڼ�¼��ͣ��λ�ã��Ա�ָ�����
	private VideoView videoView; // ��Ƶ��ͼ
	private MediaController mediaController;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.videoplayer);

		// �õ�ͼƬ�����б�
		Intent intent = getIntent();
		fileList = intent.getStringArrayListExtra("filelist");
		if (fileList == null || fileList.size() == 0)// �������Ƶ����ֱ�ӽ���
		{
			finish();
			return;
		}

		// ׼����ʼ������Ƶ
		videoView = (VideoView) findViewById(R.id.videoView);

		mediaController = new MediaController(this);
//		mediaController.setPadding(0,0,0,100);
		videoView.setMediaController(mediaController);
		videoView.setVideoPath(fileList.get(0));
		//videoView.requestFocus();

		// ע����Ƶ���Ž���ʱ�ļ�����
		videoView
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
				{
					@Override
					public void onCompletion(MediaPlayer mp)
					{
						playNext();// ������һ��
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

	@Override
	protected void onDestroy()
	{
		// setResult( 0 );//���߲���controller�����Ž���
		super.onDestroy();
	}

	/**
	 * ������һ����Ƶ
	 */
	public void playNext()
	{
		fileIndex++;
		if (fileIndex == fileList.size())// ȫ�������Ž������ͽ�����ǰactivity
		{
			finish();
		} else
		// ��ʼ������һ����Ƶ
		{
			videoView.stopPlayback();// ������һ�εĲ���
			videoView.setVideoPath(fileList.get(fileIndex));
			videoView.start();
		}
	}

}

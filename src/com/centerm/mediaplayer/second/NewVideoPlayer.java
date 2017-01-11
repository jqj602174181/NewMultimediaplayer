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

	private int fileIndex = 0; // ���ڲ��ŵ��ļ����б��е�����
	private NewFile newfile; // ��Ƶ�ļ��б�
	private boolean isPlaying = false; // �Ƿ����ڲ���
	private int playPosition = 0; // ����ͣʱ�����ڼ�¼��ͣ��λ�ã��Ա�ָ�����
	private VideoView videoView; // ��Ƶ��ͼ
	private MediaController mediaController;
	private GestureDetector mDetector; 

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.videoplayer);

		// �õ�ͼƬ�����б�
		Intent intent = getIntent();
		newfile = (NewFile)intent.getSerializableExtra("newfile");
		if (newfile == null)// �����ͼƬ����ֱ�ӽ���
		{
			finish();
			return;
		}	

		// ׼����ʼ������Ƶ
		videoView = (VideoView) findViewById(R.id.videoView);
		mDetector = new GestureDetector(this); 

		mediaController = new MediaController(this);
		//		mediaController.setPadding(0,0,0,100);
		videoView.setMediaController(mediaController);
		videoView.setVideoPath(newfile.getName());
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
		setResult( 0 );//���߲���controller�����Ž���
		super.onDestroy();
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
		return mDetector.onTouchEvent(event); //���¼�����GestureDetector.onTouchEvent() 
	}

}

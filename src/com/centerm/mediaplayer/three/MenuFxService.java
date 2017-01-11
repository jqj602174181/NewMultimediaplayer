package com.centerm.mediaplayer.three;

import com.centerm.mediaplayer.CommonActivity;
import com.centerm.mediaplayer.MyApp;
import com.centerm.mediaplayer.R;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class MenuFxService extends Service {

	//���帡�����ڲ���
	private LinearLayout mFloatLayout;
	private WindowManager.LayoutParams wmParams;
	//���������������ò��ֲ����Ķ���
	private WindowManager mWindowManager;

	private Button imageBtn;
	private Button vedioBtn;
	private Button typeBtn;
	private Button close;
	private boolean isOpen = false;

	private MyBinder binder = new MyBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(mWindowManager == null){
			createFloatView();
		} else {
			if(!isOpen){
				openFloatView();
			}else{
				closeFloatView();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void openFloatView(){
		if(mWindowManager != null && !isOpen){
			isOpen = true;
			mWindowManager.addView(mFloatLayout, wmParams);
		}
	}

	public void closeFloatView(){
		if(mWindowManager != null && isOpen){
			isOpen = false;
			mWindowManager.removeView(mFloatLayout);
		}
	}

	private void createFloatView() {
		wmParams = new WindowManager.LayoutParams();
		//��ȡWindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		//����window type
		wmParams.type = LayoutParams.TYPE_SYSTEM_ALERT; 
		//����ͼƬ��ʽ��Ч��Ϊ����͸��
		wmParams.format = PixelFormat.RGBA_8888; 
		//���ø������ڲ��ɾ۽���ʵ�ֲ���������������������ɼ����ڵĲ�����
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		//wmParams.flags = LayoutParams.FLAG_NOT_TOUCHABLE;

		//������������ʾ��ͣ��λ��Ϊ����ö�
		wmParams.gravity = Gravity.LEFT | Gravity.TOP; 

		// ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
		// wmParams.x = 0;
		// wmParams.y = 0;

		// �����������ڳ�������
		wmParams.width = 90;
		wmParams.height = 370;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		//��ȡ����������ͼ���ڲ���
		mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_menu, null);
		initView();
		openFloatView();
	}

	private void initView() {
		imageBtn = (Button)mFloatLayout.findViewById(R.id.imageBtn);
		vedioBtn = (Button)mFloatLayout.findViewById(R.id.vedioBtn);
		typeBtn = (Button)mFloatLayout.findViewById(R.id.typeBtn);
		close = (Button)mFloatLayout.findViewById(R.id.close);

		imageBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				for(CommonActivity activity : MyApp.getInstance().list){ //ȷ��ֻ��һ���б����
					activity.finish();
				}				

				Intent intent = new Intent(MenuFxService.this, ThreeActivity.class);
				intent.putExtra("type", 0);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MenuFxService.this.startActivity(intent);

				closeFloatView();
			}
		});

		vedioBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				for(CommonActivity activity : MyApp.getInstance().list){ //ȷ��ֻ��һ���б����
					activity.finish();
				}				

				Intent intent = new Intent(MenuFxService.this, ThreeActivity.class);
				intent.putExtra("type", 1);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MenuFxService.this.startActivity(intent);

				closeFloatView();
			}
		});

		typeBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				for(CommonActivity activity : MyApp.getInstance().list){ //ȷ��ֻ��һ���б����
					activity.finish();
				}				

				Intent intent = new Intent(MenuFxService.this, FourActivity.class); //���
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MenuFxService.this.startActivity(intent);

				closeFloatView();
			}
		});

		close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				closeFloatView();
			}
		});
	}

	public class MyBinder extends Binder{

		public MenuFxService getService(){
			return MenuFxService.this;
		}
	}
}

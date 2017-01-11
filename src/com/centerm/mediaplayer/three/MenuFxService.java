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

	//定义浮动窗口布局
	private LinearLayout mFloatLayout;
	private WindowManager.LayoutParams wmParams;
	//创建浮动窗口设置布局参数的对象
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
		//获取WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		//设置window type
		wmParams.type = LayoutParams.TYPE_SYSTEM_ALERT; 
		//设置图片格式，效果为背景透明
		wmParams.format = PixelFormat.RGBA_8888; 
		//设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		//wmParams.flags = LayoutParams.FLAG_NOT_TOUCHABLE;

		//调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = Gravity.LEFT | Gravity.TOP; 

		// 以屏幕左上角为原点，设置x、y初始值
		// wmParams.x = 0;
		// wmParams.y = 0;

		// 设置悬浮窗口长宽数据
		wmParams.width = 90;
		wmParams.height = 370;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		//获取浮动窗口视图所在布局
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
				for(CommonActivity activity : MyApp.getInstance().list){ //确保只有一个列表界面
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
				for(CommonActivity activity : MyApp.getInstance().list){ //确保只有一个列表界面
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
				for(CommonActivity activity : MyApp.getInstance().list){ //确保只有一个列表界面
					activity.finish();
				}				

				Intent intent = new Intent(MenuFxService.this, FourActivity.class); //类别
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

package com.centerm.mediaplayer.three;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.centerm.mediaplayer.CommonActivity;
import com.centerm.mediaplayer.MyApp;
import com.centerm.mediaplayer.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class ThreeActivity extends CommonActivity implements Notify{

	private ListView mListView;
	private MyVideoAdapter mVideoAdapter;
	private MyImageAdapter mImageAdapter;

	private String mPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private ArrayList<String> mAllArray = new ArrayList<String>();
	private ArrayList<VideoInfo> mVideoList = new ArrayList<VideoInfo>();
	private ArrayList<ImageInfo> mImageList = new ArrayList<ImageInfo>();

	private int mType = -1; //0为图片库  1为视频库

	private static final String TAG = ThreeActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mListView=(ListView) findViewById(R.id.video_listview);

		Intent intent = getIntent();
		if(intent != null){
			mType = intent.getIntExtra("type", -1);
		}

		MyApp.getInstance().list.add(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		initData();
	}

	private void initData() {
		if(mType == 0){
			String path = mPath + File.separator + "media/picture";
			ergodicFile(path); //遍历文件夹

			for(String mNewPath : mAllArray){
				ImageInfo video = new ImageInfo(mNewPath);
				mImageList.add(video);
			}
			mImageAdapter = new MyImageAdapter(mImageList, this, this);
			mListView.setAdapter(mImageAdapter);
		} else if(mType == 1){
			String path = mPath + File.separator + "media/video";
			ergodicFile(path); //遍历文件夹

			for(String mNewPath : mAllArray){
				VideoInfo video = new VideoInfo(mNewPath, R.drawable.video1);
				mVideoList.add(video);
			}
			mVideoAdapter = new MyVideoAdapter(mVideoList, this, this);
			mListView.setAdapter(mVideoAdapter);
		}

		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//				System.out.println("xxxxx:" + firstVisibleItem +","+ mAdapter.currentIndex +","+ mListView.getLastVisiblePosition());
				//				if((mAdapter.currentIndex<firstVisibleItem || mAdapter.currentIndex>mListView.getLastVisiblePosition())){
				//					System.out.println("超出");
				//					mAdapter.currentIndex = -1;
				//					mAdapter.notifyDataSetChanged();
				//				}
			}	
		});
		/*		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				currentIndex=position;
				adapter.notifyDataSetChanged();
			}
		});*/
	}

	private void ergodicFile(String path) {
		File file = new File(path);
		if(!file.exists()){
			return;
		}
		//遍历文件夹
		String[] list = file.list();
		for(String name : list){
			String newPath = path + File.separator + name;
			File newFile = new File(newPath);
			if(newFile.isFile()){
				mAllArray.add(newPath);
			}else if(newFile.isDirectory()){
				ergodicFile(newPath);
			}
		}
	}

	@Override
	public void dataChanged() {
		//		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void close() {
		ThreeActivity.this.finish();
		MyApp.getInstance().serviceConn.closeFloatView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyApp.getInstance().cleanLruCache();
		Log.i(TAG, "onDestroy");
	}
}

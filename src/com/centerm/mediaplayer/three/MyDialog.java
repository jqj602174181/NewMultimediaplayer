package com.centerm.mediaplayer.three;

import java.util.ArrayList;
import java.util.List;

import com.centerm.mediaplayer.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class MyDialog extends Dialog implements Notify{
	
	private Context context;
	private View layout;
	private List<VideoInfo> videoList=new ArrayList<VideoInfo>();
	private VideoInfo video;
	private ListView mListView;
	private MyVideoAdapter mAdapter;
	
	private String url1 = "/mnt/internal_sd/media/video/123.mp4";
	private String url2 = "/mnt/internal_sd/media/video/1080P-8M.wmv";
	private String url3 = "/mnt/internal_sd/media/video/fb-volt1080-002.flv";
	private String url4 = "/mnt/internal_sd/media/video/HD.avi";

	public MyDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		initDialog();
	}

	public MyDialog(Context context) {
		super(context, R.style.CustomAlertDialog);
		this.context = context;
		initDialog();
	}
	
	private void initDialog(){
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.activity_main, null);
		
		video = new VideoInfo(url1, R.drawable.video1);
		videoList.add(video);
		video = new VideoInfo(url2, R.drawable.video1);
		videoList.add(video);
		video = new VideoInfo(url3, R.drawable.video1);
		videoList.add(video);
		video = new VideoInfo(url4, R.drawable.video1);
		videoList.add(video);

		mListView = (ListView)layout.findViewById(R.id.video_listview);
		mAdapter = new MyVideoAdapter(videoList, context, this);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				System.out.println("xxxxx:" + firstVisibleItem +","+ mAdapter.currentIndex +","+ mListView.getLastVisiblePosition());
				if((mAdapter.currentIndex<firstVisibleItem || mAdapter.currentIndex>mListView.getLastVisiblePosition())){
					//					System.out.println("³¬³ö");
					//					mAdapter.currentIndex = -1;
					//					mAdapter.notifyDataSetChanged();
				}
			}	
		});

		this.addContentView(layout, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	@Override
	public void dataChanged() {
		
	}

	@Override
	public void close() {
		
	}

}

package com.centerm.mediaplayer.three;

import java.util.List;

import com.centerm.mediaplayer.R;
import com.centerm.mediaplayer.three.ThreeVideoPlayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

public class MyVideoAdapter extends BaseAdapter {

	private List<VideoInfo> mVideoList;
	private Context mContext;
	private Notify mNotify;
	private MyVideoThumbLoader mVideoThumbLoader;

	MediaController mMediaCtrl;
	public int currentIndex = -1;
	private ScalableVideoView mVideoView;

	public MyVideoAdapter(List<VideoInfo> videoList, Context context, Notify notify){
		this.mVideoList = videoList;
		this.mContext = context;
		this.mNotify = notify;
		this.mVideoThumbLoader = new MyVideoThumbLoader();
	}

	@Override
	public int getCount() {
		return mVideoList.size();
	}

	@Override
	public Object getItem(int position) {
		return mVideoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final int mPosition = position;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.video_item_layout, null);
			holder = new ViewHolder();
			holder.videoImage = (ImageView)convertView.findViewById(R.id.video_image);
			holder.imageButton = (ImageButton)convertView.findViewById(R.id.video_play_btn);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		String path = mVideoList.get(position).getUrl();
		holder.videoImage.setTag(path);
		mVideoThumbLoader.showThumbByAsynctack(path, holder.videoImage);

		holder.imageButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				VideoInfo info = (VideoInfo)getItem(mPosition);
				Intent intent = new Intent(mContext, ThreeVideoPlayer.class);
				intent.putExtra("mPath", info.getUrl());
				mContext.startActivity(intent);

				if(mNotify != null){
					mNotify.close();
				}
			}
		});

		return convertView;
	};

	class ViewHolder{
		ImageView videoImage;
		ImageButton imageButton;
	}
}
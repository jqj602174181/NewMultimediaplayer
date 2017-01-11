package com.centerm.mediaplayer.three;

import java.util.List;

import com.centerm.mediaplayer.R;
import com.centerm.mediaplayer.three.ThreeImagePlayer;
import com.centerm.mediaplayer.three.ThreeVideoPlayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MyImageAdapter extends BaseAdapter {

	private List<ImageInfo> mImageList;
	private Context mContext;
	private Notify mNotify;
	private MyImageThumbLoader mImageThumbLoader;

	public MyImageAdapter(List<ImageInfo> imageList, Context context, Notify notify){
		this.mImageList = imageList;
		this.mContext = context;
		this.mNotify = notify;
		this.mImageThumbLoader = new MyImageThumbLoader();
	}

	@Override
	public int getCount() {
		return mImageList.size();
	}

	@Override
	public Object getItem(int position) {
		return mImageList.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.image_item_layout, null);
			holder = new ViewHolder();
			holder.imageImage = (ImageView)convertView.findViewById(R.id.image_image);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		String path = mImageList.get(position).getUrl();
		holder.imageImage.setTag(path);
		mImageThumbLoader.showThumbByAsynctack(path, holder.imageImage);

		holder.imageImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ImageInfo info = (ImageInfo)getItem(mPosition);
				Intent intent = new Intent(mContext, ThreeImagePlayer.class);
				intent.putExtra("mPath", info.getUrl());
				mContext.startActivity(intent);

				if(mNotify != null){
					mNotify.close();
				}
			}
		});

		return convertView;
	}

	class ViewHolder{
		ImageView imageImage;
	}
}

package com.centerm.mediaplayer.three;

import java.util.List;

import com.centerm.mediaplayer.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyFirstGridViewAdapter extends BaseAdapter {

	private List<TypeItem> mList;
	private Context mContext;

	public MyFirstGridViewAdapter(List<TypeItem> list, Context context){
		this.mList = list;
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.type_item_1, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView)convertView.findViewById(R.id.image);
			holder.textView = (TextView)convertView.findViewById(R.id.text);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.imageView.setBackgroundResource(R.drawable.folder);
		String name = mList.get(position).getName();
		holder.textView.setText(name);

		return convertView;
	};

	class ViewHolder{
		ImageView imageView;
		TextView textView;
	}
}
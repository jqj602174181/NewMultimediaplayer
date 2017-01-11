package com.centerm.mediaplayer.three;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.centerm.mediaplayer.R;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class MySecondGridViewAdapter extends BaseAdapter {

	private List<String[]> mStringList;
	private Context mContext;
	private MyVideoThumbLoader mVideoThumbLoader;
	private MyImageThumbLoader mImageThumbLoader;

	public static final String IMAGE_PATH = "/mnt/internal_sd/media/picture/"; // 图片文件所在的目录
	public static final String VIDEO_PATH = "/mnt/internal_sd/media/video/"; // 视频文件所在的目录

	public static final String supportedImageFormats[] = { "gif", "bmp", "png", "jpg" };// 支持的图片文件格式
	public static final String supportedAccelerateVideoFormats[] = { "flv", "avi", "wmv", "mkv", "rm",
		"mpg", "mpeg", "divx", "swf", "dat", "3gp", "3gpp", "asf", "mov", "m4v",
		"ogv", "vob", "rmvb", "mpeg4", "mpe", "mp4v", "amv", "mp4" };// 硬件或软件已实现加速处理的视频格式

	public MySecondGridViewAdapter(List<String[]> stringList, Context context){
		this.mStringList = stringList;
		this.mContext = context;
		this.mVideoThumbLoader = new MyVideoThumbLoader();
		this.mImageThumbLoader = new MyImageThumbLoader();
	}

	@Override
	public int getCount() {
		return mStringList.size();
	}

	@Override
	public Object getItem(int position) {
		return mStringList.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.type_item_2, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView)convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		String[] value = mStringList.get(position);
		String[] newValue = new String[2];
		newValue[0] = value[0];
		newValue[1] = value[1];
		
		String name = newValue[0];
		String path = null;
		//判断图片或视频
		if(isImageFile(name)){
			path = IMAGE_PATH + name;
			mImageThumbLoader.showThumbByAsynctack(path, holder.imageView);
		}else if(isVideoFile(name)){
			path = VIDEO_PATH + name;
			mVideoThumbLoader.showThumbByAsynctack(path, holder.imageView);
		}
		newValue[0] = path;
		holder.imageView.setTag(newValue);

		return convertView;
	};

	/**
	 * 判断是否是可支持的图片文件格式
	 * 
	 * @param file
	 *            文件全路径名
	 * @return true表示支持，false表示不支持
	 */
	public Boolean isImageFile(String file)
	{
		return isSupportedFileFormat(file, supportedImageFormats);
	}

	/**
	 * 判断是否是可支持的视频文件格式
	 * 
	 * @param file
	 *            文件全路径名
	 * @return true表示支持，false表示不支持
	 */
	public Boolean isVideoFile(String file)
	{
		return isSupportedFileFormat(file, supportedAccelerateVideoFormats);
	}

	public Boolean isSupportedFileFormat(String file,
			String[] supportedFileFormats)
	{
		String ext = file.toString();
		String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);// 得到文件扩展名
		if (Arrays.asList(supportedFileFormats).contains(sub_ext.toLowerCase(Locale.ENGLISH)))
		{
			return true; // 若包含在支持的扩展名列表中，则表示支持
		}
		return false;
	}

	class ViewHolder{
		ImageView imageView;
	}
}
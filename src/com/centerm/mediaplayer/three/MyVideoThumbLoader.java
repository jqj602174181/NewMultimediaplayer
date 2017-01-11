package com.centerm.mediaplayer.three;

import com.centerm.mediaplayer.MyApp;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class MyVideoThumbLoader {

	private static final String TAG = MyVideoThumbLoader.class.getSimpleName();

	public MyVideoThumbLoader(){
		
	}    

	public void addVideoThumbToCache(String path,Bitmap bitmap){
		if(getVideoThumbToCache(path) == null){
			//当前地址没有缓存时，就添加
			MyApp.getInstance().lruCache.put(path, bitmap);
		}
	}

	public Bitmap getVideoThumbToCache(String path){
		return MyApp.getInstance().lruCache.get(path);
	}

	public void showThumbByAsynctack(String path, ImageView imgview){
		Log.e(TAG, "path:" + path);
		if(getVideoThumbToCache(path) == null){
			//异步加载
			new MyBobAsynctack(imgview, path).execute(path);
		}else{
			imgview.setImageBitmap(getVideoThumbToCache(path));
		}
	}

	class MyBobAsynctack extends AsyncTask<String, Void, Bitmap> {

		private ImageView imgView;
		private String path;

		public MyBobAsynctack(ImageView imageView,String path) {
			this.imgView = imageView;
			this.path = path;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = getVideoThumbnail(params[0]);

			if(bitmap == null){
				Log.e("MyVideoThumbLoader", "bitmap为null:" + params[0]);
			}

			if(bitmap != null){
				//加入缓存中
				if(getVideoThumbToCache(params[0]) == null){
					addVideoThumbToCache(path, bitmap);
				}
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			//通过 Tag可以绑定 图片地址和 imageView，这是解决Listview加载图片错位的解决办法之一
			String[] value = (String[])imgView.getTag();
			if(value[0].equals(path)){
				imgView.setImageBitmap(bitmap);
			}
		}
	}

	public Bitmap getVideoThumbnail(String filePath) {  
		Bitmap bitmap = null;  
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();  
		try {  
			retriever.setDataSource(filePath);  
			bitmap = retriever.getFrameAtTime();  
		}   
		catch(IllegalArgumentException e) {  
			e.printStackTrace();  
		}   
		catch (RuntimeException e) {  
			e.printStackTrace();  
		}   
		finally {  
			try {  
				retriever.release();  
			}   
			catch (RuntimeException e) {  
				e.printStackTrace();  
			}  
		}  
		return bitmap;  
	}
}
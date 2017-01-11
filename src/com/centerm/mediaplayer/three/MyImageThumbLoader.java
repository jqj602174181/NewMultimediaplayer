package com.centerm.mediaplayer.three;

import java.io.FileInputStream;

import com.centerm.mediaplayer.MyApp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class MyImageThumbLoader {

	private static final String TAG = MyImageThumbLoader.class.getSimpleName();

	public MyImageThumbLoader(){

	}    

	public void addImageThumbToCache(String path,Bitmap bitmap){
		if(getImageThumbToCache(path) == null){
			//��ǰ��ַû�л���ʱ�������
			MyApp.getInstance().lruCache.put(path, bitmap);
		}
	}

	public Bitmap getImageThumbToCache(String path){
		return MyApp.getInstance().lruCache.get(path);
	}

	public void showThumbByAsynctack(String path, ImageView imgview){
		Log.e(TAG, "path:" + path);
		if(getImageThumbToCache(path) == null){
			//�첽����
			new MyBobAsynctack(imgview, path).execute(path);
		}else{
			imgview.setImageBitmap(getImageThumbToCache(path));
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
			//			Bitmap bitmap = BitmapFactory.decodeFile(params[0]);
			Bitmap bitmap = null;
			try{
				FileInputStream fis = new FileInputStream(params[0]);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = false;
				options.inSampleSize = 5;   // width��hight��Ϊԭ����5��һ
				bitmap = BitmapFactory.decodeStream(fis, null, options);

				if(bitmap == null){
					Log.e("MyImageThumbLoader", "bitmapΪnull:" + params[0]);
				}

				if(bitmap != null){
					//���뻺����
					if(getImageThumbToCache(params[0]) == null){
						addImageThumbToCache(path, bitmap);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			//ͨ�� Tag���԰� ͼƬ��ַ�� imageView�����ǽ��Listview����ͼƬ��λ�Ľ���취֮һ
			String[] value = (String[])imgView.getTag();
			if(value[0].equals(path)){
				imgView.setImageBitmap(bitmap);
			}
		}
	}
}
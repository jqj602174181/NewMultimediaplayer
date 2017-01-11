package com.centerm.mediaplayer.three;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.centerm.mediaplayer.CommonActivity;
import com.centerm.mediaplayer.MyApp;
import com.centerm.mediaplayer.R;
import com.centerm.mediaplayer.three.Notify;
import com.centerm.mediaplayer.three.sqlite.SqlHelper;

import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class FourActivity extends CommonActivity implements Notify{

	private GridView mGridView;
	private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final String XMLPATH = SDCARD_PATH + "/config/boc_type_list.xml";
	private static final String DB_PATH = SDCARD_PATH + "/config/centerm_media_db.db";

	private List<TypeItem> mAllList = null;
	private View mHeadView;
	private TextView mTitleTextView;
	private Button mBackBtn;
	public static final String supportedImageFormats[] = { "gif", "bmp", "png", "jpg" };// 支持的图片文件格式
	public static final String supportedAccelerateVideoFormats[] = { "flv", "avi", "wmv", "mkv", "rm",
		"mpg", "mpeg", "divx", "swf", "dat", "3gp", "3gpp", "asf", "mov", "m4v",
		"ogv", "vob", "rmvb", "mpeg4", "mpe", "mp4v", "amv", "mp4" };// 硬件或软件已实现加速处理的视频格式

	private static final String TAG = FourActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_gridview);
		mGridView = (GridView)findViewById(R.id.source_gridview);
		mHeadView = findViewById(R.id.head);
		mTitleTextView = (TextView)findViewById(R.id.title);
		mBackBtn = (Button)findViewById(R.id.back);
		MyApp.getInstance().list.add(this);

		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object object = view.getTag();
				if(object instanceof MyFirstGridViewAdapter.ViewHolder){
					if(mAllList != null){
						TypeItem item = mAllList.get(position);
						Log.e(TAG, "item id:" + item.getId());
						SqlHelper.updateSql("media_title_table", item.getId());
						ArrayList<String[]> array = item.getArray();
						mHeadView.setVisibility(View.VISIBLE);
						mTitleTextView.setText(item.getName());
						MySecondGridViewAdapter adapter = new MySecondGridViewAdapter(array, FourActivity.this);
						mGridView.setAdapter(adapter);
					}
				} else if(object instanceof MySecondGridViewAdapter.ViewHolder){
					MySecondGridViewAdapter.ViewHolder holder = (MySecondGridViewAdapter.ViewHolder)object;
					String[] value = (String[])holder.imageView.getTag();
					String path = value[0];
					String id2 = value[1];
					SqlHelper.updateSql("media_file_table", id2);
					Intent intent = new Intent();
					intent.putExtra("mPath", path);
					if(isImageFile(path)){ //图片
						intent.setComponent(new ComponentName(FourActivity.this, ThreeImagePlayer.class));
					}else if(isVideoFile(path)){
						intent.setComponent(new ComponentName(FourActivity.this, ThreeVideoPlayer.class));
					}
					FourActivity.this.startActivity(intent);
					FourActivity.this.finish();
				}
			}
		});

		mBackBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mHeadView.setVisibility(View.GONE);
				mTitleTextView.setText("");
				loadGridView();
			}
		});

		loadGridView();
	}

	private void loadGridView() {
		//		File file = new File(XMLPATH);
		//		if(!file.exists()){
		//			Toast.makeText(FourActivity.this, "not found boc_type_list.xml", Toast.LENGTH_SHORT).show();
		//			FourActivity.this.finish();
		//			return;
		//		}

		//		mList = getTypeItemList(file);

		File file = new File(DB_PATH);
		if(!file.exists()){
			Toast.makeText(FourActivity.this, "not found centerm_media_db.db", Toast.LENGTH_SHORT).show();
			FourActivity.this.finish();
			return;
		}

		mAllList = getListFromDatabase(DB_PATH);

		if(mAllList == null || mAllList.size() == 0){
			Toast.makeText(FourActivity.this, "mList is empty", Toast.LENGTH_SHORT).show();
			FourActivity.this.finish();
			return;
		}

		MyFirstGridViewAdapter adapter = new MyFirstGridViewAdapter(mAllList, FourActivity.this);
		mGridView.setAdapter(adapter);
	}

	public Boolean isVideoFile(String file)
	{
		return isSupportedFileFormat(file, supportedAccelerateVideoFormats);
	}

	public Boolean isImageFile(String file)
	{
		return isSupportedFileFormat(file, supportedImageFormats);
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

	private List<TypeItem> getListFromDatabase(String path){
		try{
			SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
					path, null);
			List<TypeItem> tTypeList = new ArrayList<TypeItem>();
			Cursor cursor = database.query("media_title_table", null, null, null, null, null, null);  
			while(cursor.moveToNext()) {   
				String id = cursor.getString(cursor.getColumnIndex("id"));
				String title = cursor.getString(cursor.getColumnIndex("title")); 
				String indexs = cursor.getString(cursor.getColumnIndex("indexs")); 
				TypeItem item = new TypeItem();
				item.setId(id);
				item.setName(title);

				Cursor mCursor = database.query("media_file_table", null, "indexs=?", new String[]{indexs}, null, null, null);
				ArrayList<String[]> array = new ArrayList<String[]>();
				while(mCursor.moveToNext()) {
					String[] mPath = new String[2];
					mPath[0] = mCursor.getString(mCursor.getColumnIndex("path")); 
					mPath[1] = mCursor.getString(mCursor.getColumnIndex("id")); 
					array.add(mPath);
				}
				item.setArray(array);
				tTypeList.add(item);
				mCursor.close();
			} 
			cursor.close();
			database.close();
			return tTypeList;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	//	private List<TypeItem> getTypeItemList(File mFile) { //xml配置解析
	//		try{
	//			List<TypeItem> tTypeList = new ArrayList<TypeItem>();
	//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	//			DocumentBuilder builder = factory.newDocumentBuilder();
	//			Document document = builder.parse(new FileInputStream(mFile));
	//			Element root = document.getDocumentElement();  //获取根节点
	//			NodeList nodes = root.getElementsByTagName("type");
	//			for(int i=0; i<nodes.getLength(); i++){
	//				TypeItem typeItem = new TypeItem();
	//				Element tTypeElement = (Element)nodes.item(i);
	//				typeItem.setName(tTypeElement.getAttribute("name"));
	//				ArrayList<String> array = new ArrayList<String>();
	//				NodeList elements = tTypeElement.getElementsByTagName("item");
	//				for(int j=0; j<elements.getLength(); j++){
	//					Element tItemElement = (Element)elements.item(j);
	//					array.add(tItemElement.getAttribute("name"));
	//				}
	//				typeItem.setArray(array);
	//				tTypeList.add(typeItem);
	//			}
	//			return tTypeList;
	//		}catch(Exception e){
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}

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
				//				mAllArray.add(newPath);
			}else if(newFile.isDirectory()){
				ergodicFile(newPath);
			}
		}
	}

	@Override
	public void dataChanged() {

	}

	@Override
	public void close() {
		FourActivity.this.finish();
		MyApp.getInstance().serviceConn.closeFloatView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyApp.getInstance().cleanLruCache();
		Log.i(TAG, "onDestroy");
	}
}

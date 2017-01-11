package com.centerm.mediaplayer;

import java.util.ArrayList;

/**
 * 待执行的操作任务
 */
public class Task {
	//任务动作类别
	public static final int ACTION_PLAY = 0;	//播放的动作
	//TODO:未来可增加删除等动作
	
	
	private int action;							//任务的动作类别
	private ArrayList<String> fileList;			//文件列表对象
	private int fileType;						//文件类别
	
	public Task( int action, int fileType, ArrayList<String> fileList )
	{
		this.action = action;
		this.fileType = fileType;
		this.fileList = fileList;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public ArrayList<String> getFileList() {
		return fileList;
	}

	public void setFileList(ArrayList<String> fileList) {
		this.fileList = fileList;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

}

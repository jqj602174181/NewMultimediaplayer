package com.centerm.mediaplayer.second;

import java.util.ArrayList;

/**
 * 待执行的操作任务
 */
public class NewTask {
	//任务动作类别
	public static final int ACTION_PLAY = 0;	//播放的动作
	//TODO:未来可增加删除等动作


	private int action;							//任务的动作类别
	private NewFile newFile;			//文件列表对象
	private int fileType;						//文件类别

	public NewTask( int action, int fileType, NewFile newFile )
	{
		this.action = action;
		this.fileType = fileType;
		this.newFile = newFile;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public NewFile getNewFile() {
		return newFile;
	}

	public void setNewFile(NewFile newFile) {
		this.newFile = newFile;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

}

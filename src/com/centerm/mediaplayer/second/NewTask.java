package com.centerm.mediaplayer.second;

import java.util.ArrayList;

/**
 * ��ִ�еĲ�������
 */
public class NewTask {
	//���������
	public static final int ACTION_PLAY = 0;	//���ŵĶ���
	//TODO:δ��������ɾ���ȶ���


	private int action;							//����Ķ������
	private NewFile newFile;			//�ļ��б����
	private int fileType;						//�ļ����

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

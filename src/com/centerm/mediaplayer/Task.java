package com.centerm.mediaplayer;

import java.util.ArrayList;

/**
 * ��ִ�еĲ�������
 */
public class Task {
	//���������
	public static final int ACTION_PLAY = 0;	//���ŵĶ���
	//TODO:δ��������ɾ���ȶ���
	
	
	private int action;							//����Ķ������
	private ArrayList<String> fileList;			//�ļ��б����
	private int fileType;						//�ļ����
	
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
